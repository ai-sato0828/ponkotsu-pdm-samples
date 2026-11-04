# Java Map.ofのnullクイズ

[解説記事](https://syachiku-uz.info/java-map-of-null-nullpointerexception/)のコード枠1〜7を実行できます。JDK 17とMavenを使います。

```sh
mvn clean test
java -Dfile.encoding=UTF-8 -cp target/classes example.mapofquiz.MapOfQuiz
```

最初のクイズと変数版はMapの作成中に `NullPointerException` になります。続けて実行できるよう、デモでは例外の型を表示します。クイズ自身の `println` には到達しません。HashMapの例では `get("A")` と `get("B")` が両方とも `null` になり、`containsKey` はAが `true`、Bが `false` です。

| 記事の枠 | 配布サンプル | 実行するために追加したもの |
| --- | --- | --- |
| 1. 元のクイズ | `MapOfQuiz.quiz` | import、クラスとメソッド |
| 2. 変数に入ったnull | `MapOfQuiz.explicitNullValue` | メソッド |
| 3. HashMap | `MapOfQuiz.hashMapAllowsNull` | メソッドと戻り値。呼び出し側で `containsKey` を追加確認 |
| 4. フォームをMap.ofへ渡す | `MapOfQuiz.attributesBefore` | `UserForm` の実装、引数と戻り値 |
| 5. 可変コピー | `MapOfQuiz.mutableCopy` | メソッドと戻り値 |
| 6. Optionalを持つDTO | `MapOfQuiz.UserAttribute` と `optionalAttribute` | DTOをクラス内に配置し、入力はメソッドの引数で渡す |
| 7. 任意項目nullのJUnit | `MapOfQuizTest` | import、`mapper` と `UserForm` を実装。補足ケースを追加 |
| 8. レビューコメント | 本文を参照 | 解説文のため実行対象外 |

サンプルのマッパーでは、部署は必須とし、内線番号がnullならキーを省きます。内線番号がある場合は値をそのまま保持し、`Map.copyOf` で変更不可のMapとして返します。
