import json
import subprocess
import threading
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

class Rpc(BaseHTTPRequestHandler):
    def do_POST(self):
        request = json.loads(self.rfile.read(int(self.headers["Content-Length"])))
        assert request == {"jsonrpc": "2.0", "id": 1, "method": "eth_chainId", "params": []}
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.end_headers()
        self.wfile.write(b'{"jsonrpc":"2.0","id":1,"result":"0x7a69"}')

probe = Path(__file__).with_name("check-rpc.sh")
def invoke(port):
    return subprocess.run(["sh", probe, f"http://127.0.0.1:{port}"], text=True, capture_output=True)

with ThreadingHTTPServer(("127.0.0.1", 0), Rpc) as server:
    port = server.server_address[1]
    with ThreadingHTTPServer(("127.0.0.1", 0), Rpc) as wrong:
        wrong_port = wrong.server_address[1]
    thread = threading.Thread(target=server.serve_forever)
    thread.start()
    try:
        assert invoke(wrong_port).returncode == 7
        response = invoke(port)
        assert response.returncode == 0
        assert json.loads(response.stdout)["result"] == "0x7a69"
    finally:
        server.shutdown()
        thread.join()
assert invoke(port).returncode == 7
print("wrong port, live mock JSON-RPC, and stopped server checked")
