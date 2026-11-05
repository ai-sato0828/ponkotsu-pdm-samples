# 注文バッチに渡すListを確定時点で切り離す

JDK 17とMaven 3.9系を使う。展開先の `pom.xml` と同じ場所で `mvn test` を実行する。

- `src/main/java/OrderBatch.java` は注文の明細一覧と、共有される可変明細を定義する。
- `src/test/java/OrderBatchTest.java` は元のListへの追加・差し替え、getter経由の追加、要素自体の変更、nullを含む入力の5件を確認する。

`List.copyOf(source)` を `java.util.Collections.unmodifiableList(source)` に置き換えると、追加・差し替え・null拒否の3件が失敗する。getterから追加できないことだけでは、違いを検出できない。
