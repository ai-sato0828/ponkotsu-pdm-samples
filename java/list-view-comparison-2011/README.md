# 注文バッチに渡すListを確定時点で切り離す

JDK 17とMaven 3.9系を使う。展開先の `pom.xml` と同じ場所で `mvn test` を実行する。

- `src/main/java/OrderBatch.java` は注文の明細一覧と、共有される可変明細を定義する。
- `src/test/java/OrderBatchTest.java` は元のListへの追加・差し替え、getter経由の追加、要素自体の変更、nullを含む入力の5件を確認する。

`List.copyOf(source)` を `java.util.Collections.unmodifiableList(source)` に置き換えると、追加・差し替え・null拒否の3件が失敗する。getterから追加できないことだけでは、違いを検出できない。

## 読取専用ビューと独立した一覧を同じ入力で比較する

追加の `ListViewComparison.java` はプロジェクトrootに置いた独立実行例です。Java17のソースファイル実行を使います。既存のOrderBatch/POM/JUnitは変更していません。

```sh
java ListViewComparison.java
```

実行すると次の6行が出ます。コード内のcheckが内容と例外条件も確認し、違えばAssertionErrorで停止します。

```text
view = [A, B]
snapshot = [A]
List.copyOf(sourceWithNull) = NullPointerException
sourceWithNull after set = [A, B]
nullSnapshot = [A, null]
nullSnapshot.add = UnsupportedOperationException
```

- `Collections.unmodifiableList(source)` は元のListの読取専用ビューです。元のsourceへBを追加するとviewにも反映されます。view経由で編集できないことと、元データが変化しないことは別です。
- `List.copyOf(view)` はこの入力時点の一覧を切り離すため、後からsourceに加えたBはsnapshotに入りません。常に別instanceを新規作成するという一般保証や、要素のdeep copyを示す例ではありません。
- 要素にnullがあればList.copyOfは拒否します。仕様としてnullを残す場合の比較例は、`new ArrayList<>(sourceWithNull)` で先に構造をコピーし、それをunmodifiableListで包みます。元のnullをBへ差し替えてもコピー側はnullのまま、コピー側からのaddは拒否されます。

nullを保持する比較は、既存OrderBatchがnull要素を拒否する契約を変更するものではありません。どちらの入力を受け付けるかは用途の仕様で決めます。可変OrderLineの中身まで独立しない点は、既存5テストで引き続き確認します。

## 実行確認の数え方と範囲

`mvn test` は従来どおり5件です。追加Javaの6行/自己checkを6件のJUnitとして合算しません。Mavenはsrc配下の既存OrderBatch/Testを確認し、rootの比較Javaは上のjavaコマンドで別に実行します。

検証環境はJDK17.0.8/Maven3.9.16/JUnit5.12.2。既存キャッシュでオフライン実行し、空キャッシュ初回取得・別JDK・GUI/HTTP/DBは未確認です。旧README本文はこのファイル冒頭にそのまま保持しました。追加のLICENSEは既存リポジトリと同一のMIT文です。
