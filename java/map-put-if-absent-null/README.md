# HashMapの登録済みnullとputIfAbsent

`HashMap`で値を登録するとき、未登録のキーと、登録済みで値が`null`のキーを比較します。

JDK 17を用意し、ZIPを展開したフォルダで実行します。

```sh
java PutIfAbsentDemo.java
```

```text
A: null -> 1
B: null -> 1
C: 5 -> 5
D: true -> null
```

- Aは登録済みで値が`null`です。`putIfAbsent`の戻り値は`null`で、値が`1`に変わります。
- Bは未登録です。`putIfAbsent`の戻り値は`null`で、値に`1`が入ります。
- Cは登録済みで値が`5`です。`putIfAbsent`の戻り値も、操作後の値も`5`です。
- Dは登録済みで値が`null`です。`containsKey`が偽の場合だけ`put`するので、操作後もキーが存在し、値が`null`のままです。この方法は1スレッドで扱う`HashMap`の例です。

ZIPにはこのREADMEと`PutIfAbsentDemo.java`が入っています。
