# SpringのBeanが作れないとき、コンストラクターとinit-methodを切り分ける

同じBean名 `invoiceRules` と同じ設定値 `invoice.retry-limit` を用い、Spring Framework 6.2.19で例外の出方を比較します。コンストラクターとXMLの `init-method` は別のコンテキストで実行します。両方が同じ検証メソッドを使います。

## 実行

必要な環境はJDK 17とMavenです。展開したフォルダーで次を実行します。

```sh
mvn -q compile exec:java
mvn test
```

初回は依存関係を取得します。取得済みの環境では `-o` を加えて実行できます。実測環境はJDK 17.0.8、Maven 3.9.16、Spring Framework 6.2.19です。

## 結果の読み方

| 起動時の値 | 読み込むXML | 見る箇所 |
| --- | --- | --- |
| `0` | `constructor.xml` | 外側の `BeanCreationException` に `Failed to instantiate` が出ます。次に `BeanInstantiationException` があり、最深例外では `validateRetryLimit` の呼び出し元が `<init>` です。 |
| `0` | `init-method.xml` | 外側の `BeanCreationException` に検証メッセージが出ます。直下の `IllegalArgumentException` で、`validateRetryLimit` の呼び出し元は `initialize` です。 |
| `20` | 両方 | `started invoiceRules retryLimit=20` が出ます。 |

失敗時の最深例外は、両方とも `IllegalArgumentException: invoice.retry-limit must be positive: 0` です。デモは標準出力に例外の型・文言と最深例外の先頭2フレームを出します。Spring自身も標準エラーにコンテキスト更新の警告を出します。

`BeanCreationStageTest` の4件は、各失敗ルートでの例外の型・Bean名・検証メッセージ・呼び出し元と、値 `20` で両方が起動して設定を取得できることを確かめます。
