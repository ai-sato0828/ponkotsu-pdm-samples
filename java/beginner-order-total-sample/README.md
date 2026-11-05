# Javaで個数を変え、注文金額を確かめる

Java基礎ガイドの注文金額の練習用です。
`OrderTotal.java` には、記事に載っている `main` メソッドを `OrderTotal` クラスの中に置いています。

## 動かす

JDK 17以上を用意し、ZIPを展開してください。
ターミナルまたはコマンドプロンプトで `OrderTotal.java` があるフォルダを開きます。
`java -version` で使っているJavaの版を確認できます。

```sh
java OrderTotal.java
```

個数は最初 `2` です。`1000円` と表示されます。

## 変えて確かめる

テキストエディターで `OrderTotal.java` を開き、`int quantity = 2;` の数値だけ変えて保存し、同じコマンドを実行してください。

| 個数 | 表示 |
| --- | --- |
| 0 | 1〜10個で入力 |
| 1 | 500円 |
| 2 | 1000円 |
| 10 | 5000円 |
| 11 | 1〜10個で入力 |

[Java基礎ガイドの元のコード](https://syachiku-uz.info/java-beginner-book-guide/#main-example)
