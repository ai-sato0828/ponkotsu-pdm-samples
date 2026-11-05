# Spring Bootで受付IDの再送をDBで判定する

JDK 17、Maven 3.9系、Spring Boot 3.5.16、H2 2.3.232を使用します。DBはメモリ内で起動します。ZIPを展開し、`pom.xml` のあるフォルダで実行します。初回はMavenが依存を取得できる環境が必要です。

```sh
mvn test
```

`POST /applications` にJSONの `requestId`（UUID）と `title`（空白以外、最大120文字）を送ります。初回は201、同じIDと件名の再送は200で先に保存された受付を返します。同じIDで異なる件名は409で、先の件名を置き換えません。別のIDは別件です。MockMvcと実際のH2を使う6件のテストを収録しています。

アプリを手元で起動する場合は次のコマンドを使用します。

```sh
mvn spring-boot:run
```

```sh
curl -i http://localhost:8080/applications \
  -H 'Content-Type: application/json' \
  -d '{"requestId":"00000000-0000-4000-8000-000000000001","title":"備品購入申請"}'
```

同じcurlを2回実行すると、201の後に200で同じ受付を受け取ります。再送時にUUIDを作り直すと別件になります。メモリDBのためアプリの終了で内容が消えます。単一の社内受付を題材とした認証なしのサンプルです。実運用の認可、利用者やテナント単位のキー、DBごとの差、外部通知の重複対策は別途設計します。
