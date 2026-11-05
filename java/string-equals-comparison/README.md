# Javaの文字列比較を注文CSVで試す

JDK 17を用意し、ZIPを展開して `OrderStatusComparison.java` のあるフォルダを開きます。依存ライブラリ、CSVファイル、DBは不要です。

```sh
java OrderStatusComparison.java
```

最初の2行では、どちらも文字は `CANCELLED` です。ソースに直書きした値は `== true / equals true`、注文A-104から読んだ値は `== false / equals true` になります。A-105は `PAID`、A-106は空欄を `null` にした入力で、どちらも取消の候補に入りません。

- `CANCELLED.equals(status)` を `status == CANCELLED` に戻して実行します。A-104の取消判定が `false` になることを確かめ、元に戻します。
- `CANCELLED.equals(status)` を `status.equals(CANCELLED)` に変えて実行します。A-106で `NullPointerException` が起きます。確認したら元に戻します。
- 後半では、台帳とCSVの両方が空欄の場合を比較します。`Objects.equals` は `true`、記録済みの状態が一致したかの判定は `false` です。

サンプルは学習用の2列です。引用符、カンマを含む項目やCSVの読み込み処理は扱いません。
