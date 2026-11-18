# Map.putの戻り値と登録結果を確認する

`Map.put`の戻り値は、操作前にそのキーへ入っていた値です。今回入れた値ではありません。
このサンプルでは、値に`null`を入れられる`HashMap<String, Integer>`を使います。

## 実行環境

- JDK 17
- Maven（確認時は3.9.16）
- JUnit Jupiter 5.12.2

## Map.putの4ケースをテストする

ZIPを展開し、`pom.xml`があるフォルダーで実行します。

```sh
mvn clean test
```

`src/test/java/MapPutReturnValueTest.java`のJUnitテスト4件が成功します。
各テストは別の`HashMap`を作り、キーの有無・値・要素数を操作の前後で確認します。

| 操作前のA | 操作 | putの戻り値 | 操作後のA |
| --- | --- | --- | --- |
| 未登録（containsKey=false） | put("A", 1) | null | 1 |
| 5を登録済み | put("A", 1) | 5 | 1 |
| nullを登録済み（containsKey=true） | put("A", 1) | null | 1 |
| 未登録（containsKey=false） | put("A", null) | null | null（containsKey=true） |

3件目は「すでに入っているnullを1へ置き換える」操作です。
4件目は「まだないキーに、今回nullを入れる」操作です。
4件目では`get("A")`は操作前後とも`null`ですが、`containsKey("A")`は`false`から`true`へ変わります。
戻り値や`get`が`null`であることだけでは、未登録や登録失敗とは判断できません。

## 既存のputIfAbsentとcontainsKeyの例を実行する

`PutIfAbsentDemo.java`は、従来の配布版から変更せず収録しています。
同じフォルダーで、Javaのソースファイル実行を使います。

```sh
java PutIfAbsentDemo.java
```

出力は次の4行です。これはJUnitの件数とは別の、従来の実行例です。

```text
A: null -> 1
B: null -> 1
C: 5 -> 5
D: true -> null
```

- A：登録済みの値がnullなので、`putIfAbsent`で1に変わる。
- B：キーがないので、`putIfAbsent`で1が入る。
- C：すでに5があるので、`putIfAbsent`では5が残る。上の`put`の上書きとは異なる。
- D：`containsKey`で存在を確認し、登録済みnullを変更せず残す。

この例は単一スレッドの`HashMap`での動作確認です。他の`Map`実装がnullを許すことや、
`containsKey`と`put`を組み合わせた処理の並行実行時の原子性を保証するものではありません。

## 収録ファイル

- `pom.xml`：テストの依存と実行設定
- `src/test/java/MapPutReturnValueTest.java`：Map.putの4テスト全文
- `PutIfAbsentDemo.java`：従来の4出力の実行例
- `README.md`：実行手順と期待値
- `LICENSE`：MIT License
