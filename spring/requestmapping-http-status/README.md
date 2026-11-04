# Spring MVCの@RequestMappingをMockMvcで再現する

JDK 17とMaven 3.9系で、展開後のこのフォルダから次を実行します。

```sh
mvn clean test
```

Spring Boot 3.5.16の依存管理とSpring Framework 6.2.19を使います。初回はMavenリポジトリからの依存取得が必要です。

`SampleController` は POST 専用のJSON・フォーム受信用、`GetOnlyController` はGET専用の別例です。テストは別のMockMvcへ登録するので、POST専用の方へGETすると405になり、レスポンスのAllowはPOSTになります。両方を同じアプリケーションへ登録したときは、この405の前提が変わります。

テスト14件で、JSONとフォームのハンドラ選択、405・415・406・404、Content-Typeなし、Acceptなしとワイルドカード、壊れたJSONの400、指定を省略した場合のJSON変換、GET専用の2パスを検証します。Spring Securityと独自例外処理は組み込んでいません。動かすのはMockMvcのテストで、実HTTPサーバーは起動しません。

[ブログ記事](https://syachiku-uz.info/spring-mvcrequestmapping/)
