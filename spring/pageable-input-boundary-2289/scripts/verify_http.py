#!/usr/bin/env python3
"""少数の固定データを使い、localhost JARのHTTP境界だけを比較する。"""
import json
import os
from pathlib import Path
import queue
import re
import subprocess
import sys
import threading
import time
import urllib.error
import urllib.request


def verify(jar, baseline=False):
    java = str(Path(os.environ["JAVA_HOME"]) / "bin/java") if "JAVA_HOME" in os.environ else "java"
    process = subprocess.Popen(
        [java, "-jar", str(jar), "--server.address=127.0.0.1", "--server.port=0"],
        stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True,
    )
    messages = queue.Queue()
    log = []
    def read_output():
        for line in process.stdout:
            log.append(line)
            messages.put(line)
    reader = threading.Thread(target=read_output, daemon=True)
    reader.start()
    results = []
    try:
        port = None
        seeded = baseline
        deadline = time.monotonic() + 55
        while time.monotonic() < deadline:
            if process.poll() is not None:
                raise RuntimeError("JAR exited before startup: " + "".join(log[-20:]))
            try:
                line = messages.get(timeout=0.2)
            except queue.Empty:
                continue
            match = re.search(r"Tomcat started on port (\d+)", line)
            if match:
                port = int(match[1])
            if "SEED_READY open=3 closed=1" in line:
                seeded = True
            if port is not None and seeded:
                break
        else:
            raise TimeoutError("JAR startup or seed did not finish")
        cases = [("?page=42949673&size=50", 500)] if baseline else [
            ("", 200), ("?page=2&size=2", 200),
            ("?page=42949672&size=50", 200), ("?page=42949673&size=50", 400),
            ("?page=2147483647&size=1", 200), ("?page=2147483647&size=50", 400),
        ]
        for query, expected_status in cases:
            url = f"http://127.0.0.1:{port}/inquiries{query}"
            try:
                response = urllib.request.urlopen(url, timeout=8)
            except urllib.error.HTTPError as failure:
                response = failure
            with response:
                body = json.load(response)
                assert response.status == expected_status, (query, response.status, body)
                if not baseline and expected_status == 200:
                    assert body["totalElements"] == 3
                    assert body["hasNext"] is False
                    if query:
                        assert body["items"] == []
                    else:
                        assert [row["id"] for row in body["items"]] == [3, 2, 1]
                        assert body["page"] == 0 and body["size"] == 20
                results.append({"query": query, "status": response.status, "body": body})
    finally:
        process.terminate()
        try:
            process.wait(timeout=10)
        except subprocess.TimeoutExpired:
            process.kill()
            process.wait(timeout=5)
        reader.join(timeout=2)
        if os.environ.get("PAGEABLE_PROBE_LOG"):
            Path(os.environ["PAGEABLE_PROBE_LOG"]).write_text("".join(log))
    return {"baseline": baseline, "requests": results, "serverStopped": process.poll() is not None,
            "externalDbOrApi": False, "largeDatasetGenerated": False}


if __name__ == "__main__":
    jar_path = Path(sys.argv[1] if len(sys.argv) > 1 else "target/pageable-input-boundary-2289-1.0.0.jar").resolve()
    print(json.dumps(verify(jar_path, baseline="--baseline" in sys.argv[2:]), ensure_ascii=False, indent=2))
