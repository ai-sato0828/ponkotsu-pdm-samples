# HashMap.getのnullを3状態・キャッシュ・キーから調べる

## 実行

Java 17、Maven 3.9系。検証はJDK17.0.8 / Maven3.9.16 / JUnit5.12.2です。Spring・外部DB・ネット上のAPIは使いません。

ZIPを展開しpom.xmlがある場所で実行します。

```sh
mvn clean test
java -cp target/classes info.syachiku.hashmap.Main
```

30テスト成功。初回Maven依存取得にはネット接続が必要です。検証では既存依存キャッシュをofflineで使用しました。空キャッシュの初回取得、Windows、他JDKは未確認です。

## 同じMapで3状態を比較

scoreFixture: Carol=80、Alice=nullを登録、Bobは未登録です。

| name | get | containsKey | getOrDefault(-1) | describeScore |
| --- | --- | --- | --- | --- |
| Carol | 80 | true | 80 | 点数: 80 |
| Alice | null | true | null | 採点前 |
| Bob | null | false | -1 | 受験者なし |

H01〜H12は同じfixtureの4列×3行を検査します。H13はlabelFixtureのA=null/B未登録/C=登録値に対し、getOrDefaultとdefaultIfNullの違いを検査します。defaultIfNullはA/Bを意図的に未設定へ統合します。H14は採点済みだけ保存するgradedOnlyで、nullを省き0を残します。

## 検索結果は同じ、呼出し回数は違う

UserRepository.findByIdの契約はnullではなくOptionalを返すことです。CountingUserRepositoryは固定のMapとID別回数だけを持ちます。

- H15: ID2未検出を3回、nullableCacheは毎回Optional.emptyを返しRepository3回、キーは新規登録されない。
- H16: 同じID2を3回、optionalCacheは毎回Optional.emptyを返しRepository1回、MapにID2→Optional.emptyを保持。
- H17: ID1が存在する時はどちらも同じUserを返し、Repository1回。
- H18: もともとID2→nullを登録していた場合は、null計算後もキーが存在しRepository3回。『null計算なら必ずキーが消える』とは言えない。

本例は1スレッドで短い間に3回呼ぶだけの比較です。TTL、失効、更新伝播、メモリ上限、並列安全性、障害のキャッシュを実装・検証したものではありません。Optional.emptyは検索済み0件を意味し、接続エラーを同一視しません。

## 型の契約

ScoreResultはGRADEDに数値必須、NOT_GRADEDは数値なし、status nullは禁止。H19〜H23で有効値と矛盾状態拒否を確認します。点数の0〜100制限は元の記事にないため追加していません。ScoreBoardはMap値へnullを登録しない境界を作り、H24で元と同じ3表示、H25でnull name/result拒否を確認します。内部Mapを外へ公開しません。NOT_GRADEDのscore()はnullですが、Map値のScoreResult自体はnullにしない設計です。

## キーの3比較

- IdentityKey: Objectの参照同一性を使う意図的な不良例。同じ社員番号でも別インスタンスは一致しない（H26）。equals/hashCodeの契約違反という意味ではなく、必要な業務上の等価性を定義していない。
- MutableKey: equals/hashCodeが社員番号を使うが、put後E001→E002へ変更できる不良例（H28）。Map仕様は等価性に影響するキー変更後を規定しません。JDK17.0.8でget=null/containsKey=false/size=1を観測し、その教材データの検査にしています。全Map・全JDKで必ずnullになるという保証ではありません。
- EmployeeKey: 不変recordへ修正。同社員番号の別インスタンスでもequals/hashCode一致、get=Aliceを確認（H27/H29）。

containsKey=falseだけでキー設計が原因とは断定できません。未登録を確認した後、検索キーの値と型、equals/hashCode、put後の変更を調べます。

H30はConcurrentHashMapとMap.ofのnull拒否を別々に検査。HashMapでnullキー/値を登録できることも比較します。

## 故障注入と復元

検証用コピーでUserCaches.findWithOptionalCacheのreturnだけを`return repository.findById(userId);`へ変更し、次を実行します。

```sh
mvn test '-Dtest=MapExamplesTest#H16*'
```

同じ検索結果でもRepository呼出しがexpected1/actual3となり失敗します。元のcomputeIfAbsentへ戻し、mvn clean testで全30件成功へ戻してください。配布ZIPは正常版です。全型・main・テスト・POMを含みtargetや依存キャッシュは含みません。
