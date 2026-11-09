# SQL文字列連結とバインドを同じDBで比較する

Java 17以上・Maven 3.9系。検証環境はJDK 17.0.8、Maven 3.9.16、Spring JDBC 6.2.19、H2 2.3.232、JUnit 5.12.2です。Spring Boot、Docker、外部DB、ポート待受、実データは使いません。H2はJVM内だけの一時DBで、プロセス終了時に消えます。初回のMaven依存取得にはネット接続が必要です。空キャッシュからの初回取得、他DB製品、Windowsでは未検証です。

## 最初の実行

ZIPを展開し、pom.xmlのある場所で実行します。

```sh
mvn clean test
```

26件（通常の@Testが18件、S17のnull入力8件）が成功します。テストでは実際のH2を使い、空IN・nullはDBへ接続しないことも確認します。モックの戻り値を検査したものではありません。各テストに独立した5行のDBを作るため、前のケースの追加データは残りません。

まず、次の3テストだけを実行して違いを読むこともできます。外側の引用符も含めてコピーします。

```sh
mvn test '-Dtest=UserRepositoryTest#S01*+S02*+S03*'
```

| ID | 入力 | 不良連結版 | バインド版 |
|---|---|---|---|
| S01 | Alice | U002 | U002 |
| S02 | O'Brien | BadSqlGrammarException | U001 |
| S03 | ' OR '1'='1 | U001〜U005の全5行 | 0行 |

元記事: https://syachiku-uz.info/java-sql-string-concatenation-injection/

## ファイルの役割

- `schema.sql` / `data.sql`: id/name/email/status/created_atの表と架空の5行。U001=O'Brien、U002=Alice、U003=Cara、U004=100%Real、U005=A_B。
- `User.java`: 全検索共通のid/name/email。recordの`id()`等で値を読みます。
- `UserRepository.java`: JdbcTemplate、NamedParameterJdbcTemplate、LIKE、許可リストORDER BY、IN。
- `PlainJdbcUserRepository.java`: Connection/PreparedStatement/ResultSetを閉じながらUserへ読み取る実装。
- `UnsafeUserRepository.java`: ローカル比較だけに使う意図的な不良例。外部システムや業務データで使わないでください。SELECTだけです。
- `SampleDatabase.java`: テスト用の独立したH2 DBを作成。
- `UserRepositoryTest.java`: S01〜S19の仕様と期待値。S17は8入力なので26件になります。
- `CountingDataSource.java`: 接続取得回数を記録。空INは空結果だけでなく接続取得0も検査します。
- `ComparisonRun.java`: 開発時の観測補助。src/test内にあり、実行にはtest-classesとMaven依存一式を含むclasspathが必要です。記事の読者は上記Mavenテストで同じ差を確認できます。
- `excerpts/logging.java.txt`: 既存loggerに渡す説明用の抜粋。実行対象のJavaファイルではありません。

## 入力の契約

- name完全一致: nullはNullPointerExceptionとしてDB接続前に拒否。空文字は今回のfixtureでは0件。trimや大小文字変換はしません。格納nameはVARCHAR(64)。64文字を保存・検索するケースと、未登録の65文字を検索して0件になるケースがあります。入力長を理由に独自例外を追加していません。
- status/from: statusは文字列、fromはLocalDate、DATE列を`>=`で比較するため当日を含みます。不明statusは0件。nullは接続前に拒否。名前付きと位置指定で同じ結果です。
- LIKE: `%keyword%`を値としてバインドします。`%`と`_`はワイルドカードです。空文字は`%%`になり全5行。文字どおりの`%`だけを探す仕様ではありません。リテラル検索に変える場合は、採用DBのESCAPE句とエスケープ文字自身を含む仕様・試験を別途追加してください。
- ORDER BY: `name`と`createdAt`だけを固定列に変換し、不明値/nullはid。方向はbooleanでASC/DESCの二択。入力文字列そのものはSQLへ入りません。同値時はid ASCで順序を固定します。H2で`ORDER BY ?`へ文字列nameをバインドした観測はDataIntegrityViolationExceptionでした。他DBも同じ例外になるとは扱いません。
- IN: 空Listは接続取得0で空List。重複値は重複行を作りません。未登録IDは返りません。List自身または要素のnullは接続前に拒否します。

## 追加ケース

S04=生JDBCの引用符/条件式、S05=日付境界、S06=名前付きstatus、S07〜S11=LIKE/空文字、S12=64/65文字、S13〜S14=固定候補と不正sort、S15〜S16=IN、S17=null8入力、S18=未知status/最終日翌日、S19=生JDBCnullを確認します。例外名と件数はH2の検証版に対するものです。

## 修正を壊してテストが落ちることを確認する

必ず配布フォルダのコピーで試します。実装と期待値を同時に変えないでください。

1. `UserRepository.findByName`のreturnを次へ置換し、S02だけ実行します。

```java
return jdbcTemplate.query("SELECT id, name, email FROM users WHERE name = '" + name + "' ORDER BY id", USER_MAPPER);
```

```sh
mvn test '-Dtest=UserRepositoryTest#S02*'
```

引用符のSQLが壊れてS02がエラーになります。元の`return jdbcTemplate.query(sql, USER_MAPPER, name);`へ戻します。

2. `findByIds`の`if (userIds.isEmpty()) { return List.of(); }`だけを削除し、S16だけ実行します。

```sh
mvn test '-Dtest=UserRepositoryTest#S16*'
```

H2では空INの結果が空でも、接続を1回取ったためexpected 0/actual 1で失敗します。ガードを戻し、最後に`mvn clean test`で全26件を成功へ戻します。

## 仕様を確認した一次資料

- https://docs.spring.io/spring-framework/reference/6.2/data-access/jdbc/core.html
- https://docs.oracle.com/javase/tutorial/jdbc/basics/prepared.html
- https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html

独自サンプルと検証結果であり、実案件の認証・権限・トランザクション・接続プールや全DB方言を検証する教材ではありません。
