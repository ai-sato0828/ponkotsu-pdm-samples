# Java PDFレスポンス: inlineとattachmentを同じPDFで比較する

JDK17以上、Maven3.9系が必要です。掲載中の `PdfResponse.send` は無変更です。
JasperReportsの帳票生成処理は含みません。生成後のPDFをどう返すかに限定し、
有効な1ページのPDF `src/main/resources/sample.pdf` を入力として同梱しています。

## 実行とテスト

ZIPを展開し、このREADMEとpom.xmlがあるディレクトリで実行します。

```sh
java -version
mvn test
```

JUnitは組込Tomcatを127.0.0.1の空きポートで起動し、Java HttpClientでGETします。
`/preview` は `inline; filename="sample.pdf"`、`/download` は
`attachment; filename="sample.pdf"` を返します。両方でHTTP200、
Content-Type `application/pdf`、レスポンス本文とfixtureの完全一致を検証します。
成功時は `Tests run: 2, Failures: 0, Errors: 0, Skipped: 0` と表示されます。
サーバーはテスト終了時に停止します。ローカルTCP通信の許可が必要です。

初回はMaven依存を取得します。依存取得済みの環境なら `mvn -o test` で実行できます。
検証環境では既存依存だけでオフライン実行しました。空のMavenキャッシュからの初回取得は未確認です。

## ブラウザとcurlで確認

```sh
mvn compile exec:java -Dexec.args=8080
```

起動したまま `http://127.0.0.1:8080/preview` と
`http://127.0.0.1:8080/download` をブラウザで開きます。
8080が使用中なら `-Dexec.args=8081` などへ変更してください。
サーバーは127.0.0.1にのみbindします。終了はCtrl+Cです。

別ターミナルで、同じプロジェクトのディレクトリから実行します。

```sh
curl --fail --silent --show-error -D preview.headers -o preview.pdf http://127.0.0.1:8080/preview
curl --fail --silent --show-error -D download.headers -o download.pdf http://127.0.0.1:8080/download
cat preview.headers
cat download.headers
cmp src/main/resources/sample.pdf preview.pdf
cmp preview.pdf download.pdf
```

`cmp` が何も出力せず終了コード0なら完全一致です。
Content-Dispositionはpreviewがinline、downloadがattachment、Content-Typeは両方application/pdfです。
ブラウザのPDF表示設定によってはinlineでもダウンロードされます。
この場合は開発者ツールでレスポンスヘッダーを確認してください。
本サンプルの自動検証は実HTTPヘッダーと本文bytesまでです。
Edge等で実際にプレビュー表示されるか、ブラウザ設定変更は検証していません。

## コードの対応

- `src/main/java/PdfResponse.java`: 記事の掲載コード全文。preview引数で返すヘッダーだけを切り替え、渡されたPDFを書き出します。
- `src/main/java/PdfServer.java`: 共通fixtureを読み込み、/previewでtrue、/downloadでfalseを渡す実HTTP入口。
- `src/test/java/PdfResponseTest.java`: 2経路のHTTPステータス・Content-Type・Content-Disposition・本文を検証。
- `src/main/resources/sample.pdf`: ReportLabで作成した1ページPDF。JasperReportsによる生成例ではありません。
- `pom.xml`: コンパイル・実行・JUnitの依存と設定。

Tomcat10.1.55、JUnit5.12.2を使用します。Tomcatのannotations API同梱依存は除外し、
同じJakarta APIを提供するjakarta.annotation-api2.1.1を明示しています。
Spring・Mockito・JasperReportsは不要です。Servlet APIは組込Tomcatが提供します。
JDK17では停止時にTomcatのメモリーリーク検出用add-opens警告が出ることがあります。
本サンプルでHTTP比較の失敗やサーバー停止失敗を示すものではありません。
本番向けの配信基盤や認可、ファイル名の外部入力は扱いません。
