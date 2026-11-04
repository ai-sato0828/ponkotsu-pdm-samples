# 複数の通知BeanをSpringで選ぶ

JDK 17とMaven 3.9系で `mvn test` を実行します。Spring Framework 6.2.19の実コンテキストをテストごとに起動します。Spring Bootアプリ、Webサーバー、メール配信先、チャット接続は不要です。

| 場面 | 検証内容 |
|---|---|
| 候補が二つで選定材料なし | コンテキストのrefreshが失敗。原因がNoUniqueBeanDefinitionExceptionで、候補がメールとチャットの2件 |
| @Primary | 通常の注文通知がMAIL:order-42 |
| @Qualifier | メール側に@Primaryがあっても、incident-channelで障害通知がCHAT:incident-7 |
| List | メールとチャットの両方を受け取る。@Orderの値でメール、チャットの順 |

`SenderConfigurations.WithoutDefault` と `WithDefault` は比較用の別構成です。同じコンテキストに両方を登録しません。通知を実送信する代わりに、選ばれた実装を戻り値で確認します。
