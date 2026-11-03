# 実行確認（2026-11-04）

- Java 17.0.8 / Maven 3.9.5 / Spring Framework 6.2.19 / JUnit 5.12.2。
- `mvn -o -B test`：6件成功、失敗・エラー・スキップなし。
- `mvn -o -B org.codehaus.mojo:exec-maven-plugin:3.6.3:java -Dexec.mainClass=info.syachiku.bean.BeanLookupDemo`：B01〜B05を実行し、候補数1/0/0/2/2を確認。
- 意図的な変更の確認：別コピーの`scan.xml`を誤った探索先に変更し、B01が期待候補数1と実際0の不一致を検出した。配布元は正常な探索先のまま。
- 既存依存キャッシュを使用。空の依存キャッシュ、別JDK/OS、Webアプリ全体は未確認。
