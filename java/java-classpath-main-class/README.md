# Java 17でパッケージ付きのメインクラスを起動する

1,200円の商品2個と350円の商品1個を足して、標準出力に `2750` を出すサンプルです。起動するクラスは `OrderTotal`、金額を計算する補助クラスは `OrderLine` です。どちらも `example.orders` パッケージにあります。

## コンパイルして起動する

JDK 17を用意し、展開したフォルダーで実行します。外部ライブラリは不要です。

```sh
java -version
javac -version
mkdir -p out
javac -d out src/example/orders/OrderLine.java src/example/orders/OrderTotal.java
java -cp out example.orders.OrderTotal
```

出力

```text
2750
```

`javac -d out` で、次の2ファイルができます。

```text
out/
└── example/
    └── orders/
        ├── OrderLine.class
        └── OrderTotal.class
```

クラスパスのディレクトリは `example/` の親にある `out` です。起動名には、パッケージ名を含めた `example.orders.OrderTotal` を指定します。Javaはこの指定から `out/example/orders/OrderTotal.class` を探します。

## エラーが出る指定と探す場所

`out` が空の状態で `java -cp out example.orders.OrderTotal` を実行すると、ファイルがまだ無いので終了コード1です。コンパイルに成功した後は `out/example/orders/OrderTotal.class` ができたことを確認できます。

コンパイル後に下のコマンドを実行すると、いずれも終了コード1になります。エラーの英語表示はOracle JDK 17.0.8で確認しました。

| コマンド | 指定から探すパス | エラーの原因行に出る文字 |
| --- | --- | --- |
| `java -cp out OrderTotal` | `out/OrderTotal.class` | `ClassNotFoundException: OrderTotal` |
| `java -cp out/example/orders example.orders.OrderTotal` | `out/example/orders/example/orders/OrderTotal.class` | `ClassNotFoundException: example.orders.OrderTotal` |
| `java -cp out/example/orders OrderTotal` | `out/example/orders/OrderTotal.class` | `NoClassDefFoundError: example/orders/OrderTotal (wrong name: OrderTotal)` |

3行目はファイルを見つけても、Javaが求めた `OrderTotal` とクラスファイルに記録された `example.orders.OrderTotal` が食い違います。

英語表示の1行目と原因行は、単純名の例なら次のとおりです。

```text
Error: Could not find or load main class OrderTotal
Caused by: java.lang.ClassNotFoundException: OrderTotal
```

言語を揃えて試す場合は `-Duser.language` と `-Duser.country` を `java` の直後に指定します。Oracle JDK 17.0.8で確認した日本語表示は次のとおりです。

```sh
java -Duser.language=ja -Duser.country=JP -cp out OrderTotal
```

```text
エラー: メイン・クラスOrderTotalを検出およびロードできませんでした
原因: java.lang.ClassNotFoundException: OrderTotal
```

## Java 17のソースファイル実行で確認する

空のクラスパスを指定し、起動するソースファイルだけを渡します。

```sh
mkdir -p empty-classpath
java -cp empty-classpath src/example/orders/OrderTotal.java
```

Java 17のこの実行条件では、隣の `OrderLine.java` は自動コンパイルされず、`cannot find symbol`（`variable OrderLine`）で終了コード1になります。冒頭の手順で両方をコンパイルすると起動できます。

## 6ケースをまとめて確認する

```sh
sh check.sh
```

Java 17とPOSIXシェルを使います。未コンパイル、単純名、クラスパスの起点違い、ファイル内の名前と指定の不一致、正常起動、ソースファイル実行を確認します。成功すると各ケースの `PASS` と実測結果を表示します。一時ディレクトリ内で検証するため、作業中の `out` は変更しません。
