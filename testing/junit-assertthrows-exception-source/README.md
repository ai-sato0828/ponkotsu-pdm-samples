# JUnitのassertThrowsで例外を投げる操作を絞る

JDK 17以上とMavenを使います。初回の依存取得にはネット接続が必要です。展開後、pom.xmlのあるディレクトリで実行します。

```sh
mvn clean test
```

正常版は4件成功します。MisleadingLabelTestは、郵便番号の準備が先に例外を投げるため、重量の上限を確認できないことを観測する1件です。実務で重量の上限を検証するコードはLabelServiceTestの3件を参考にしてください。

LabelServiceから `if (grams > MAX_GRAMS)` のブロックだけを外して再実行すると、準備の例外を拾うテストは成功し、2001gを拒否するテストだけ失敗します。元に戻してからLabelServiceTestの `rejectsParcelAboveTheMaximum` の準備に不正な郵便番号を指定すると、準備の行で例外となり、テスト全体は成功になりません。

この例はラベル文字列を返す練習です。配送業者の実際の重量上限や住所の実在確認は扱いません。
