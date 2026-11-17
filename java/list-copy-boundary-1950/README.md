# Listコピーの境界：List本体、User、User内のtags

Java17、Maven3.9系、JUnit5.12.2。このサンプルはSpring/DB/外部サービスを使いません。

```sh
mvn test
mvn -q exec:java
```

検証環境Java17.0.8/Maven3.9.16。既存キャッシュでoffline実行しています。空のキャッシュから初回に依存を取る検証はしていません。必要なプラグインやJUnitがなければMaven Centralへの接続が必要です。

## 同じUserモデルで境界を比べる

`User(String id, String name, List<String> tags)`。getId/getName/setName/getTagsを使います。Userのname参照はsetNameで変更でき、tagsは可変Listです。この教材ではsource List・User・tagsを非nullとし、tagsの要素は不変のStringとして扱います。任意の型/循環参照を再帰的にコピーする汎用機能ではありません。

Userコンストラクタは渡されたtags参照を保持します。教材で「共有する」「独立させる」を区別するため、UserLists側がどのListを渡すか明示します。防御的コピーを常に行う不変モデルの実装例ではありません。

| 正式メソッド | Listの構造 | User | tags List | コピー側name変更 | コピー側tags変更 |
|---|---|---|---|---|---|
| copyStructureOnly | 別の可変List | 共有 | 共有 | 元へ伝わる | 元へ伝わる |
| copyUsersSharingTags | 別の可変List | 別 | 共有 | 元へ伝わらない | 元へ伝わる |
| copyForEditing | 別の可変List | 別 | 別 | 元へ伝わらない | 元へ伝わらない |
| copyReadOnlyStructure | 変更不可の構造snapshot | 共有 | 共有 | 元へ伝わる | 元へ伝わる |

copyForEditingはid/name/tagsの値と順序を保ったまま、このモデルで編集する可変部分を独立させます。id/nameと各タグのStringは不変なので、同じString参照を共有して構いません。「すべての参照を別にする」ことが目的ではありません。

List.copyOfの戻り値が常に新しいオブジェクトとは仮定しません。追加・削除・置換ができないことと、元Listの構造変更が影響しないことを確認し、Userの可変性とは分けます。List.copyOfはnull要素を受け付けませんが、null境界の試験は今回の10件に含めていません。

## JUnit10件

1. 構造コピーはListだけ別でUser/tagsは共有。
2. 構造コピー後のadd/removeが両方向で独立。
3. 構造コピーのname変更は両方向へ伝わる。
4. Userだけのコピーは全フィールド値を保持するがtagsは共有。
5. Userだけのコピーはname変更が両方向で独立。
6. Userだけのコピーではtags変更が両方向へ漏れる。
7. 編集用コピーは全フィールド値を保持し、User/tagsを別にし、不変Stringだけ共有する。
8. 編集用コピーのname/tags変更が両方向で独立。
9. 編集用コピーのList add/removeも両方向で独立。
10. copyOfはadd/removeを拒否し、元の構造変更を反映しないが、User/name/tagsの変更は共有する。

テストは各ケースで新しい2ユーザーを用意します。U001/田中/tags[general,pending]、U002/鈴木/tags[support]。Mainは出力を短くするためU001の初期tagsを[general]とし、各比較を新しいデータで始めます。テストとMainのタグ数の違いを省略しないでください。

## Mainの4行

構造コピーで佐藤へ改名したとき、元も佐藤だが元の件数2/コピー3。Userだけのコピーは元名田中を保つがtagsのreviewが両方へ入る。編集用コピーは元名田中・元tags[general]を保つ。readOnlyへaddはUnsupportedOperationException、Userの改名は元へ伝わる。出力の同一性は固定の真偽値で、オブジェクトのアドレス等は出力しません。

## 抜粋と全文

User.javaはuserModel、UserLists.javaはstructureCopy/elementCopy/editingCopy/readOnlyCopy、Main.javaはstructureDemo/elementDemo/editingDemo/readOnlyDemoのBEGIN/ENDを持ちます。記事で抜粋するときはこの領域と対応させ、全文は同じファイルから確認できます。

収録はUser.java、UserLists.java、Main.java、UserListsTest.java、POM、README、LICENSE。依存JAR、class、個人設定は含みません。旧2ファイルのListCopyDemoや旧ZIPは変更せず、この記事の新しい統一APIに対応する独立moduleです。

Java17公式仕様：
https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/List.html#copyOf(java.util.Collection)
https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/ArrayList.html#%3Cinit%3E(java.util.Collection)
