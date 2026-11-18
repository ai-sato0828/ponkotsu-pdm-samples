# JDBCでSQLのNULLをJavaのBooleanに残す

機能フラグを読むサンプルです。`active` に `TRUE`（有効）、`FALSE`（無効）、`NULL`（未設定）を入れます。テーブルは接続内のH2メモリDBだけに作成します。

## 実行

JDK 17・Maven 3.9系を用い、`pom.xml` があるフォルダーで実行します。

```sh
mvn clean test
mvn -q exec:java
```

```text
id=1 getBoolean=true wasNull=false afterGetInt=false getObject=true restored=true
id=2 getBoolean=false wasNull=false afterGetInt=false getObject=false restored=false
id=3 getBoolean=false wasNull=true afterGetInt=false getObject=null restored=null
```

`wasNull` は `getBoolean("active")` の直後、`afterGetInt` は非NULLのIDを `getInt("id")` で読んだ直後です。3行目の `wasNull` が、別の列を読む前と後で変わります。`restored` は、最初に保存した `wasNull` の値から復元した `Boolean` です。

```java
boolean primitive = rows.getBoolean("active");
boolean wasNullForActive = rows.wasNull();
Boolean restored = wasNullForActive ? null : Boolean.valueOf(primitive);
```

対象DBのJDBCドライバーで変換をサポートしていれば、`rows.getObject("active", Boolean.class)` でも3通りを保てます。H2 2.3.232でこの動作を確認し、JUnit Jupiter 5.12.2の4件で保持・読み取り順・比較の結果を検証しています。`Boolean.TRUE.equals(value)` は有効な場合だけ実行する判定に使えます。未設定と明示的な無効を区別したい処理では、`value == null` を別に判定します。

- [Java 17のResultSet.getBoolean](https://docs.oracle.com/en/java/javase/17/docs/api/java.sql/java/sql/ResultSet.html#getBoolean(java.lang.String))
- [Java 17のResultSet.wasNull](https://docs.oracle.com/en/java/javase/17/docs/api/java.sql/java/sql/ResultSet.html#wasNull())
- [Java 17の型付きResultSet.getObject](https://docs.oracle.com/en/java/javase/17/docs/api/java.sql/java/sql/ResultSet.html#getObject(java.lang.String,java.lang.Class))
