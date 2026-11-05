# 同じAccountMapperをプレビューと取込で使う実習

JDK 17を使います。ZIPを展開し、このREADMEと `src` が見える場所で次を実行します。外部ライブラリ、DB、画面は使いません。

```sh
javac -encoding UTF-8 src/Account.java src/AccountMapper.java src/ClassImporter.java src/PreviewVerification.java
java -cp src PreviewVerification
```

| 入力 | プレビュー | プレビューの保存回数 | 取込 | 取込の保存回数 |
|---|---|---:|---|---:|
| 100 | 100の行を表示するための値を返す | 0 | プレビューと同じ値を保存担当へ渡す | 1 |
| 101 | 「契約数は100以下」で拒否 | 0 | 同じ理由で拒否 | 0 |
| 100の次に101 | 同じ理由で拒否 | 0 | 正常な先頭行も保存担当へ渡さない | 0 |

`PreviewVerification.java` の `previewAccounts` は変換結果を返し、保存は呼びません。取込では `ClassImporter` が全行を変換してから保存担当を呼びます。両方に同じ `AccountMapper(100)` のインスタンスを渡しているため、上限の判定は同じです。ここでいう保存回数は検証用の関数の呼び出し回数です。実DBの書き込みは行いません。

`Account.java`、`AccountMapper.java`、`ClassImporter.java`、MITライセンスは、記事で案内しているGitHubの固定版からそのまま収録しています。既存の39件版は別の実習で、分割前・privateメソッド版・別クラス版の取込結果を比較します。

固定コミット `6bda150916e34e07ae24aac65e61fd89c8d1771a` の [元のソース](https://github.com/ai-sato0828/ponkotsu-pdm-samples/tree/6bda150916e34e07ae24aac65e61fd89c8d1771a/design/method-or-class) · [39件版のZIP](https://github.com/ai-sato0828/ponkotsu-pdm-samples/raw/6bda150916e34e07ae24aac65e61fd89c8d1771a/downloads/method-or-class-sample.zip)
