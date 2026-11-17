#!/usr/bin/env python3
"""起動済みのローカルJARへ、仕様表と同じ28 POSTを送る。標準ライブラリのみ。"""
from pathlib import Path
from urllib.parse import urlparse
from urllib.request import Request, urlopen
from urllib.error import HTTPError
import json
import sys

root = Path(__file__).resolve().parents[1]
base = (sys.argv[1] if len(sys.argv) > 1 else 'http://127.0.0.1:18087').rstrip('/')
target = urlparse(base)
if target.scheme != 'http' or target.hostname != '127.0.0.1':
    raise SystemExit('この確認用スクリプトは http://127.0.0.1 のサンプルにだけ送信します。')
cases = json.loads((root/'src/test/resources/input-cases.json').read_text())
names = {'not-null':'NotNull','not-empty':'NotEmpty','not-blank':'NotBlank','required-name':'RequiredName'}
rows = []
for case in cases:
    for endpoint, constraint in names.items():
        request = Request(base+'/validation/'+endpoint,
                          data=json.dumps(case['body'], ensure_ascii=False).encode('utf-8'),
                          headers={'Content-Type':'application/json; charset=utf-8','Accept':'application/json'},
                          method='POST')
        try:
            response = urlopen(request, timeout=10)
        except HTTPError as error:
            response = error
        with response:
            status = response.code
            body = json.loads(response.read())
        expected = case['expected'][endpoint]
        assert status == expected, (case['id'],endpoint,status,expected,body)
        if status == 400:
            assert len(body['errors']) == 1,body
            assert body['errors'][0]['field'] == 'name',body
            assert body['errors'][0]['constraint'] == constraint,body
        else:
            assert body['name'] == case['body']['name'],body
        rows.append({'case':case['id'],'endpoint':endpoint,'expected':expected,
                     'actual':status,'response':body})
print(json.dumps({'baseUrl':base,'posts':len(rows),'allMatched':True,'rows':rows},ensure_ascii=False,indent=2))
