# Cookieの設定をstatic factoryへまとめる実行サンプル

対象記事: https://syachiku-uz.info/cookie-static/

記事に掲載した `CookieLogic.java` と `JSESSIONID.java` を、そのままの内容で収録しています。追加したテストと小さなServlet入口から、生成するCookieの属性、インスタンスを共有しないこと、セッションを残さないことを確認します。

## 実行環境

- JDK 17
- Maven 3.9系
- Tomcat Embed 10.1.55 / Jakarta Servlet 6.0
- JUnit Jupiter 5.12.2

依存バージョンとMavenプラグインのバージョンは `pom.xml` に固定しています。検証にはJDK 17.0.8とMaven 3.9.16を使用しました。依存が未取得の環境では、最初のMaven実行時にMaven Centralへの接続が必要です。検証端末は依存取得済みで、空の依存キャッシュからの初回取得は未確認です。

## 一式をテストする

ZIPを展開して `pom.xml` があるフォルダで実行します。

```sh
mvn test
```

成功時は `Tests run: 6, Failures: 0, Errors: 0, Skipped: 0` と表示されます。

| テスト | 確認すること |
|---|---|
| CookieFactoryTest.oldFactoryReturnsTheExpectedExpirationCookie | 旧式のCookieLogicも、名前・Path・Domain・有効期限・Secure・HttpOnlyを満たす |
| CookieFactoryTest.namedFactoryReturnsTheSameContractWithItsOwnType | 専用型のfactoryへ設定を移しても、応答へ渡すCookieの初期属性は同じ |
| CookieFactoryTest.factoryDoesNotReuseAMutableCookieBetweenResponses | 1個目のPathを変えても、次に生成したCookieへその変更が漏れない |
| LogoutHttpTest.checkingWithoutASessionReturns401AndDoesNotIssueACookie | セッションなしの保護ページ要求は401で、セッションもCookieも新規発行しない |
| LogoutHttpTest.logoutWithoutASessionExpiresTheCookieButCreatesNoSession | セッションなしのログアウトは204と削除Cookieだけを返し、セッション作成・破棄とも0回 |
| LogoutHttpTest.oldSessionIdCannotAccessTheProtectedResourceAfterLogout | セッション発行201→保護ページ200→ログアウト204→同じ旧IDでは401。再ログアウトしても新規発行しない |

HTTPテストはTomcatを `127.0.0.1` の空きポートで起動し、JDKのHttpClientから実際にリクエストします。Servlet APIを独自のスタブへ置き換えたテストではありません。テストのたびにTomcatを終了し、一時ディレクトリを削除します。

factoryだけを先に試す場合は `mvn -Dtest=CookieFactoryTest test`、HTTPを含む3件は `mvn -Dtest=LogoutHttpTest test` です。

## 手動でHTTPの応答を見る

```sh
mvn compile exec:java -Dexec.args=0
```

`Cookie demo: http://127.0.0.1:53649/` のように、実際に使うポートが表示されます。以下の `53649` は、そのポートへ置き換えます。終了はCtrl+Cです。

1. `curl -i http://127.0.0.1:53649/protected` → 401。
2. `curl -i -X POST http://127.0.0.1:53649/session` → 201。`Set-Cookie` の先頭に出る `JSESSIONID=...` を控えます。
3. `curl -i -H 'Cookie: JSESSIONID=手順2のID' http://127.0.0.1:53649/protected` → 200。
4. `curl -i -X POST -H 'Cookie: JSESSIONID=手順2のID' http://127.0.0.1:53649/logout` → 204と削除Cookie。
5. 手順3を同じIDでもう一度実行 → 401。

手順4のヘッダーには次の属性が入ります。属性の順序や `Expires` の表記だけで判定せず、各属性を確認してください。

```text
Set-Cookie: JSESSIONID=; Max-Age=0; ...; Path=/; Secure; HttpOnly
```

Domain属性は付けません。記事と同じく、名前 `JSESSIONID`、Path `/`、Domain未指定の構成です。別Path・DomainのCookieを削除する汎用ライブラリではありません。

## このHTTPサンプルで確認できる範囲

ローカルのHTTPで確認するのは、サーバーが生成するヘッダーと、サーバー側セッションの状態です。クライアントはCookieヘッダーを**手動で**送っています。Secure属性のCookieをブラウザがHTTPでも自動送信・削除すると確認したものではありません。実アプリでは記事の前提どおりHTTPSで返し、ブラウザのCookie状態を別途確認します。

`POST /session` は検証用の架空ユーザーをセッションへ入れる入口です。パスワード認証や本番用ログイン機能ではありません。Spring Securityなどがセッションとログアウトを管理している場合は、その機構の設定を使用してください。

## 設計として読み取る点

`static` にしているのはfactoryメソッドです。可変の `Cookie` インスタンスを `static` フィールドで共有しているわけではありません。呼び出すたびに新しい `JSESSIONID` を返し、名前と初期属性を1か所へ集めます。

また、専用型を返すだけでCookieの取り違えをすべて防げるわけではありません。`HttpServletResponse.addCookie` の引数は `Cookie` なので、他のCookieも渡せます。用途を型で制限したいAPIでは、そのAPIの引数にも専用型を使うなど、呼び出し側まで含めて設計する必要があります。今回のテストが保証するのは、初期属性と生成・ログアウトの挙動です。

## 収録ファイル

```text
pom.xml
README.md
src/main/java/CookieLogic.java
src/main/java/JSESSIONID.java
src/main/java/CookieDemoServer.java
src/test/java/CookieFactoryTest.java
src/test/java/LogoutHttpTest.java
```

`CookieDemoServer` は記事の関数を呼ぶ検証用の入口です。アプリ全体をSpring Bootへ移行する例や、Cookieを継承する設計を全アプリへ推奨する例ではありません。
