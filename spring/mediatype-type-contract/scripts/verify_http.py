#!/usr/bin/env python3
"""記事用JARを127.0.0.1の空きポートで検証し、必ず終了する。"""
import json
import os
from pathlib import Path
import queue
import re
import subprocess
import sys
import threading
import time
import urllib.request


def verify(jar):
    java = str(Path(os.environ["JAVA_HOME"]) / "bin/java") if "JAVA_HOME" in os.environ else "java"
    process = subprocess.Popen(
        [java, "-jar", str(jar), "--server.address=127.0.0.1", "--server.port=0"],
        stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True,
    )
    lines = queue.Queue()
    startup_log = []
    def read_output():
        for line in process.stdout:
            startup_log.append(line)
            lines.put(line)
    reader = threading.Thread(target=read_output, daemon=True)
    reader.start()
    results = []
    try:
        port = None
        deadline = time.monotonic() + 40
        while time.monotonic() < deadline:
            if process.poll() is not None:
                raise RuntimeError("JAR exited before startup: " + "".join(startup_log[-20:]))
            try:
                line = lines.get(timeout=0.2)
            except queue.Empty:
                continue
            match = re.search(r"Tomcat started on port (\d+)", line)
            if match:
                port = int(match[1])
                break
        if port is None:
            raise TimeoutError("JAR startup did not finish")
        for item, quantity in [("BOOK-001", 1), ("PEN-002", 3), ("用紙-003", 2)]:
            expected = {"itemCode": item, "quantity": quantity, "status": "preview"}
            request = urllib.request.Request(
                f"http://127.0.0.1:{port}/order-previews",
                data=json.dumps({"itemCode": item, "quantity": quantity}, ensure_ascii=False).encode("utf-8"),
                headers={"Content-Type": "application/json", "Accept": "application/json"},
                method="POST",
            )
            with urllib.request.urlopen(request, timeout=5) as response:
                actual = json.load(response)
                assert response.status == 200
                assert response.headers["Content-Type"] == "application/json"
                assert actual == expected, (actual, expected)
                results.append({"method": "POST", "path": "/order-previews", "status": response.status,
                                "contentType": response.headers["Content-Type"], "body": actual})
        request = urllib.request.Request(f"http://127.0.0.1:{port}/order-previews/example",
                                         headers={"Accept": "application/json"})
        with urllib.request.urlopen(request, timeout=5) as response:
            actual = json.load(response)
            assert response.status == 200
            assert response.headers["Content-Type"] == "application/json"
            assert actual == {"itemCode": "BOOK-001", "quantity": 1, "status": "preview"}
            results.append({"method": "GET", "path": "/order-previews/example", "status": response.status,
                            "contentType": response.headers["Content-Type"], "body": actual})
    finally:
        process.terminate()
        try:
            process.wait(timeout=10)
        except subprocess.TimeoutExpired:
            process.kill()
            process.wait(timeout=5)
        reader.join(timeout=2)
    return {"requests": results, "serverStopped": process.poll() is not None,
            "databaseOrExternalApiUsed": False}


if __name__ == "__main__":
    jar_path = Path(sys.argv[1] if len(sys.argv) > 1 else "target/mediatype-type-contract-1.0.0.jar").resolve()
    print(json.dumps(verify(jar_path), ensure_ascii=False, indent=2))
