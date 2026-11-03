# 検証結果（2026-11-04）

- Java 17.0.8、Maven 3.9.5、Spring Boot 3.5.16、Spring Framework 6.2.19、Thymeleaf 3.1.5.RELEASE、Hibernate Validator 8.0.3.Final、JUnit 5.12.2。
- `mvn -o -B package`：MockMvc 8件、失敗0、エラー0、スキップ0。起動用JARを生成。
- 同じ空欄POSTの故障版・修正版で、`userForm.name`のエラーはともに1件。故障版では文言なし、修正版では「名前を入力してください。」を表示。どちらもメール値を保持し、保存Serviceは呼ばない。
- 別コピーの通常テンプレートを故障版の`th:errors="*{email}"`へ戻し、`blankNameShowsJapaneseMessageAndKeepsEmailWithoutSaving`だけを実行。モデルのエラー確認の後、HTMLの文言を確認するassertionが期待どおり失敗（1件中1件）。配布ソースにはこの変更を適用していない。
- 起動用JARを127.0.0.1で起動。初期GET 200、名前空欄POST 200と日本語エラー・メール保持、正常POST 302、一覧GET 200と正常登録1件を実際のHTTPで確認。応答は`text/html;charset=UTF-8`。
- 既存のMaven依存キャッシュを利用。未取得のValidation starter等はMaven Centralから取得。空の依存キャッシュ、別OS/JDK、実DB、認証/CSRF、ブラウザCSS/JavaScriptによる見え方は未検証。
