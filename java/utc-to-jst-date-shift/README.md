# JavaでUTCをJSTに変換する

JDK 17以上とMavenを使います。初回は依存取得にネット接続が必要です。展開後、pom.xmlのあるディレクトリから実行します。

```sh
mvn clean test
mvn -DargLine=-Duser.timezone=UTC clean test
mvn -DargLine=-Duser.timezone=America/Los_Angeles clean test
```

各回で5件が成功します。UTCの12月31日15:00:00は、東京では1月1日00:00:00です。修正前の `BrokenSalesDay` は、UTCの時計表示を日本時間として解釈し直すため、集計日も瞬間も変わります。修正版の `SalesDay` は、入力の `Instant` を保ったまま東京での暦日を取り出します。

修正版の `paidAt.atZone(TOKYO)` を `LocalDateTime.ofInstant(paidAt, ZoneOffset.UTC).atZone(TOKYO)` に変えると、年越しの日付・時刻・瞬間を確かめる4件が失敗します。

この例は、決済の発生時刻を受け取り、日本時間0時で暦日を区切る仕様です。企業独自の営業日ルール、タイムゾーンが分からない現地時刻の補完、DB保存型の選定は対象外です。
