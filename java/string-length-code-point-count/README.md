# JavaのString.lengthとコードポイント

連携先が「表示名をUnicodeコードポイント3個まで」と定めた練習用サンプルです。
必須項目のためnull、空文字、空白だけは拒否し、前後の空白は除去しません。
UTF-8から正しくデコードされた入力を前提にしています。

## 実行

JDK 17、Maven 3.9系を利用します。プロジェクトを展開して以下を実行します。

```sh
mvn clean test
java -Dfile.encoding=UTF-8 -cp target/classes example.displayname.DisplayNameDemo
```

`𠮷田葵` について修正前は `before=false`、修正後は `after=true` です。
`DisplayNameDemo` はUTF-16コード単位、コードポイント、拡張書記素クラスタ、UTF-8バイトも比較します。
端末の書体によって表示できない文字があっても、JUnitは文字列と真偽値で比較します。

- DisplayNameValidator.java は修正版です。
- DisplayNameDemo.java は修正前の判定と、入力ごとの比較結果を表示します。
- DisplayNameValidatorTest.java は上限と空欄、修正前の不具合を計14件で確認します。
