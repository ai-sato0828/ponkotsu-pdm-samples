# JPAの独自値型をString一列へ保存する

架空のPostalCode値型をEntityのフィールドへそのまま置くと、この採用環境ではEntityManagerFactory作成時にJdbcTypeRecommendationExceptionが起きます。修正版は同じ値型をStringへ対応付けるAttributeConverterと明示@Convertを使います。外部DB/API/HTTPは使わず、プロセス内H2だけで確認します。

## 実行

JDK17とMaven3.9系で、READMEとpom.xmlがあるフォルダーから実行します。

```sh
mvn test
```

JUnitは12件（未変換起動失敗1、値型/Converter単体8、実H2保存・復元3）です。起動失敗はBrokenMappingTestが期待する例外として捕捉するため、正しい配布版のテスト全体は成功します。

失敗ケースだけを読む場合は次を実行します。

```sh
mvn -Dtest=BrokenMappingTest test
```

## 修正前に何が失敗するか

UnconvertedAddressのPostalCodeフィールドには@Convertがありません。PostalCodeはSerializableを実装しないrecordです。値型のJavaオブジェクトをどのJDBC型へ渡すかが決まらず、EntityManagerFactoryの作成に失敗します。Entityをpersistする前の問題です。

採用版での最深例外とmessageは次のとおりです。

```text
org.hibernate.type.descriptor.java.spi.JdbcTypeRecommendationException
Could not determine recommended JdbcType for Java type 'example.postal.PostalCode'
```

メッセージ内の型名は本サンプルの完全修飾名です。Hibernateの内部例外型・文言は採用版での実測として扱います。このエラーのすべての型へ同じ変換を当てる説明ではありません。ここではPostalCodeの値をString一列で表す契約を先に決めています。

## 修正版の値と列の契約

- PostalCode.valueはASCII数字7桁のみ。0123456の先頭0を保持します。実在の郵便番号や地域の確認はしません。
- 6/8桁、英字、全角数字、ハイフンはIllegalArgumentException。trim、補完、ハイフン除去、整数化はしません。
- PostalCode(null)はNullPointerExceptionで拒否します。
- Address.postalCode自体は任意項目としてnullを許し、SQL NULLへ保存し、読み戻しもnullです。値型の内部value=nullとは区別します。
- Addressのpostal_code列はVARCHAR(7)であり、@Convert(PostalCodeConverter.class)を明示します。
- converterはPostalCode→valueのString、String→new PostalCodeの変換だけを行います。自動で他のフィールドへ適用しません。

## DBの文字列と復元後を別々に確認する

PostalPersistenceTestは@Transactionalのテストではありません。書込用EntityManagerを開いてpersist→commit→closeし、JDBCで実列の0123456・JDBC型VARCHAR・長さ7を確認します。その後、別のEntityManagerを作り、異なるEntityインスタンスから同じPostalCodeが復元されたことを確認します。2nd level/query cacheは無効です。

同じPersistence Contextのfindで、最初から持っているJavaオブジェクトを見て『復元できた』と扱う試験ではありません。H2の接続URLも実接続metadataでjdbc:h2:mem:converted-postalであることを確認します。

## 不正なDB値をnullへ隠さない

テストではJDBCで7文字の12A4567を直接保存します。VARCHAR(7)には入りますが、PostalCodeのASCII数字7桁契約には違反します。新しいEntityManagerで取得すると、converterがnew PostalCodeへ戻す段階で拒否されます。

採用版の外側例外はPersistenceException、最深原因は自分で定義したIllegalArgumentExceptionです。エラーをcatchしてnullへ置き換えず、不正な永続化データとして扱います。失敗した読取TXはfinallyでrollbackし、EntityManagerをcloseします。既存データの補正・移行はこのサンプルでは実装しません。

## 収録と実行範囲

- PostalCode: 非Serializableの値型。
- broken/UnconvertedAddress: 意図的な修正前のEntity。
- fixed/Address、PostalCodeConverter: 修正後のEntityと一列変換。
- PostalApplication: 修正後packageだけをentity/component scanする起動クラス。
- BrokenMappingTest: 修正前packageだけを別構成でscanし、起動失敗を確認。
- PostalCodeConverterTest / PostalPersistenceTest: 入力・null・DB往復・不正復元を検証。

修正前と修正後を同じZIPに置くためscan対象を分離しています。起動失敗側のURLはテスト起動引数でjdbc:h2:mem:broken-postalへ固定し、修正版のDBと分けています。

確認環境: Java17.0.8 / Maven3.9.16 / Spring Boot3.5.16 / Hibernate6.6.53.Final / H2 2.3.232 / JUnit5.12.2。既存依存キャッシュでオフライン実行。空キャッシュからの初回依存取得、他provider/DB/JDK、HTTPは未確認です。

収録は対象Java、設定、JUnit3ファイル、POM、README、MIT LICENSEです。故障検出コピーや外部サービス接続設定は含みません。
