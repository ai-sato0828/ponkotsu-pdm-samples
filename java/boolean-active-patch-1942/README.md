# BooleanのPATCH：省略・明示null・falseを区別する補助サンプル

Javaの`Boolean`記事のうち「JSONの項目省略とfalseを区別する」節の補助教材です。
既存の`ConsentRegistration`（同意登録6テスト）とは別のプロジェクトです。
このZIPは、JSON文字列の確認から、利用者の有効状態の更新、保存呼び出しまでを38ケースで検証します。

## 環境と実行

- JDK 17以上、Maven 3.9系
- Jackson 2.19.0、JUnit Jupiter 5.12.2
- 初回はMaven Centralから依存関係を取得できるネット接続が必要です。
- DB、Spring、Webサーバー、外部APIは不要です。

ZIPを展開し、`pom.xml`があるフォルダーで実行します。

```sh
mvn clean test
```

成功時は`Tests run: 38, Failures: 0, Errors: 0, Skipped: 0`です。
`P01-F`はP01を初期値false、`P01-T`は初期値trueで試す意味です。
各ケースは新しい利用者と記録用Repositoryで開始し、前ケースの更新を引き継ぎません。

## 更新の契約

|ID|JSON入力|期待する結果|有効状態|保存呼び出し|
|---|---|---|---|---|
|P01|`{}`|`UNCHANGED`|初期値を保持|0回|
|P02|`{"active":false}`|`UPDATED`|false|1回、falseを渡す|
|P03|`{"active":true}`|`UPDATED`|true|1回、trueを渡す|
|P04|`{"active":null}`|`ACTIVE_NOT_BOOLEAN`|初期値を保持|0回|
|P05|`{"active":"false"}`|`ACTIVE_NOT_BOOLEAN`|初期値を保持|0回|
|P06|`{"active":0}`|`ACTIVE_NOT_BOOLEAN`|初期値を保持|0回|
|P07|`{"active":[]}`|`ACTIVE_NOT_BOOLEAN`|初期値を保持|0回|
|P08|`{"active":{}}`|`ACTIVE_NOT_BOOLEAN`|初期値を保持|0回|
|P09〜P13|`[]`、`null`、`true`、`1`、`"false"`|`ROOT_NOT_OBJECT`|初期値を保持|0回|
|P14|`{"active":`|`INVALID_JSON`|初期値を保持|0回|
|P15|空文字列|`INVALID_JSON`|初期値を保持|0回|
|P16|`{} {}`|`INVALID_JSON`|初期値を保持|0回|
|P17|`{"active":false,"active":true}`|`INVALID_JSON`|初期値を保持|0回|
|P18|Javaのnull（入力文字列なし）|`INVALID_JSON`|初期値を保持|0回|
|P19|空白だけの入力文字列|`INVALID_JSON`|初期値を保持|0回|

上記19入力を初期値false・trueの両方で実行します（19×2＝38ケース）。
`P10`のJSON値`null`と`P18`のJavaのnullは別です。
有効な`active`が指定された場合は、元と同じ値でも保存を1回呼ぶ教材の契約です。
値が変わったときだけ保存する最適化は扱っていません。
`active`以外の項目はこの教材の更新対象ではなく、存在しても変更しません。

`body.has("active")`で存在を先に調べ、存在する場合だけ`isBoolean()`を確認します。
`hasNonNull`にすると、明示的なnullが省略と同じ扱いになるため、この契約を守れません。
`asBoolean()`で文字列や数値を変換してから調べる方法も使いません。
JSON文書は1件とし、後続の別JSONや重複キーを拒否するようObjectMapperを設定しています。

## ファイルと責務

- `src/main/java/info/syachiku/booleanpatch/ActivePatchService.java`：JSON確認、状態更新、Repository呼び出し。
- `src/test/java/info/syachiku/booleanpatch/ActivePatchServiceTest.java`：仕様の入力と期待値、初期値2状態での38ケース。
- `pom.xml`：実行するJavaの世代、Jackson、JUnit、Mavenプラグインの版。
- `README.md`：この手順書。

保存先は渡されたbooleanをリストへ記録するテストダブルです。
保存回数と値を確かめていますが、実DBのコミット、HTTP応答、同時更新、認可、保存失敗時の原子性は検証していません。
既存のDTO例は、省略と明示nullの両方を「未変更」にしてよい別契約です。
この補助サンプルを、そのDTO例と同じ契約だと扱わないでください。

## 誤った変更を検出する

検証用コピーで`!body.has("active")`だけを`!body.hasNonNull("active")`へ変えて、再びテストします。
P04-FとP04-Tは、例外ではなく未変更として処理されるため失敗します。
ほかの入力の結果が同じでも、明示nullの扱いが変わったことを検出できます。
確認後は`has`へ戻し、全38ケースを成功させます。

元記事：<https://syachiku-uz.info/java-boolean-unboxing-nullpointerexception/>
