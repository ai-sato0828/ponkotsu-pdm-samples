# オフセット付きの日時から、どの日付を取り出すか

海外の注文システムから受信した日時を日付で集計する例です。画面に入力元の日付を表示するときは、受信した `OffsetDateTime` に対して `toLocalDate()` を呼びます。日本時間の日付で集計するときは、同じ瞬間を `atZoneSameInstant(ZoneId.of("Asia/Tokyo"))` で変換してから日付を取り出します。

JDK 17とMaven 3.9系で、ZIP内の `offset-timestamp-date-sample` フォルダーを開いて実行します。

```sh
mvn -B -ntp test
java -cp target/classes example.OffsetTimestampDateDemo
```

出力は次の4行です。

```text
LocalDateTime.parse: DateTimeParseException errorIndex=19
2026-02-28T23:30:00Z writtenDate=2026-02-28 tokyoDate=2026-03-01
2026-03-01T08:30:00+09:00 writtenDate=2026-03-01 tokyoDate=2026-03-01
sameInstant=true
```

`errorIndex=19` は、この入力の `Z` が0から数えて19番目にあることを示します。`LocalDateTime.parse` の既定の形式はオフセットのない日時です。`OffsetDateTime.parse` は `Z` と `+09:00` の両方を解釈します。

JUnitは、既定のローカル日時パーサーがオフセットで止まる2件、入力に書かれた日付を取り出す2件、同じ瞬間と日本時間の日付を照合する1件、日本時間の午前0時前後の2件を実行します。変換先は `Asia/Tokyo` に固定します。検証では取得済みのMavenキャッシュを使い、`mvn -B -o -ntp clean test` で実行しました。

[Java 17のOffsetDateTime.atZoneSameInstant](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/OffsetDateTime.html#atZoneSameInstant(java.time.ZoneId))
