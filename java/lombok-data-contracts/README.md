# Lombok @Dataの出力と等価性

架空の利用者情報で、ログ出力とHashSet検索の故障を比較します。実在のユーザーやパスワード、実際のハッシュは使用していません。JPA／Spring／DBに依存しないサンプルです。

必要環境: Java 17、Maven 3.9系。Lombok 1.18.36、JUnit 5.12.2をpom.xmlで固定しています。

    mvn -B test
    java -cp target/classes info.syachiku.samples.value.LombokContractsDemo

- UnsafeUser: @Dataの既定生成。表示名・ハッシュを等価性に含み、出力にも含める故障の実演用。
- ExcludedUser: @ToString.Excludeのみ。出力から外れても等価性には含む。
- SafeUser: 割当済みで変わらないuserIdのみを比較し、toStringの許可項目も別途明示。
- ReferenceUser: getter/setterのみ。値に基づく等価性は定義しない。
- LombokContractsTest: 故障の観察3回、修正後の契約5回、除外・参照同一性・ID setterなし各1回の合計11回。

## 生成後のJavaを確認する

pom.xmlのある場所で実行します。Mavenキャッシュの場所を変えている場合は環境変数MAVEN_REPO_LOCALで指定してください。

    sh generate-sources.sh
    mkdir -p target/delombok-classes
    javac --release 17 -d target/delombok-classes generated/*.java
    java -cp target/delombok-classes info.syachiku.samples.value.LombokContractsDemo

同梱generated/はdelombok -f prettyで得た出力です。表示用の装飾を抑えるオプションを使用し、等価性や出力処理は手修正していません。生成後Java単体でもデモ出力が一致します。

Java 17／Maven 3.9.5と既存の依存キャッシュで確認。別Lombok版、初回依存取得、JPAの採番やプロキシは未確認です。
