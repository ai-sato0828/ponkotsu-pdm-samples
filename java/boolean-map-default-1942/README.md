# MapのgetOrDefaultとBooleanのnullを比較する

JDK17とMaven3.9系で、pom.xmlのあるディレクトリから実行します。

```sh
mvn clean test
```

純JavaのHashMap<String,Boolean>を使い、未登録・登録済みnull・false・trueの4状態を比較します。対象全文はsrc/main/java/BooleanMapReader.java、テスト全文はsrc/test/java/BooleanMapReaderTest.javaです。JUnitのparameterized testが4件実行されます。

`readWithDefault`の`flags.getOrDefault(key, Boolean.FALSE)`は、キーが未登録ならfalseを返します。しかしキーにnullが登録されていればnullを返し、戻り値をbooleanへ変換する箇所でNullPointerExceptionになります。テストではgetOrDefault自体の戻り値nullと、booleanを返すメソッドでのNPEを別々にassertします。HashMapはnull値を保持できます。Map.ofはこの入力を作れないため使いません。

今回の採用仕様は「true以外は実行しない」です。`readTrueOnly`の`Boolean.TRUE.equals(flags.get(key))`にすると、未登録・null・falseをfalse、trueだけをtrueにします。未登録と明示nullとfalseを区別して再入力を求める仕様には、この修正を使いません。3状態を保存する他サンプルやJSON PATCHの欠落判定を置き換えるものではありません。メソッドはMapを書き換えません。

```text
MAP case=absent containsKey=false get=null before=false fixed=false
MAP case=stored-null containsKey=true get=null before=NullPointerException fixed=false
MAP case=stored-false containsKey=true get=false before=false fixed=false
MAP case=stored-true containsKey=true get=true before=true fixed=true
```

ログのgetはmap.get(key)であり、getOrDefaultの戻り値ではありません。absentとstored-nullはgetの結果が同じnullでも、containsKeyとgetOrDefaultの結果が異なります。不具合を再現するassertThrowsも成功したJUnitケースに含まれます。

実行環境はJDK17.0.8、Maven3.9.16、JUnit Jupiter5.12.2、Surefire3.5.6です。既存依存キャッシュを使うオフライン実行を確認し、空cacheからの初回依存取得は未確認です。DB・HTTP・外部サービスは使いません。nullのMap引数・並行Map・同時更新は範囲外です。

独立コピーでreadTrueOnlyをreadWithDefault呼び出しへ戻すと、stored-nullケースだけNPEで失敗します。復元後は4件成功します。旧1942の3配布（6/38/10件）は変更せず、この4件とは別集計です。
