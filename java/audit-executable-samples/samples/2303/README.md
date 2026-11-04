JDK 17以上とMavenで実行します。`clean`を入れ、切替前のテストクラスを消します。

```sh
mvn -Pbroken -Dtest='SharedStateTest#first' clean test
mvn -Pbroken -Dtest='SharedStateTest#second' clean test
mvn -Pbroken clean test
mvn clean test
```

失敗版では各メソッド単独で成功し、全件では1件だけ失敗します。どちらのメソッドが後に動くかは前提にしません。修正版では全件成功します。
