# CSVの先頭数量がBOMで数値変換できない

JDK17とMaven3.9系で、pom.xmlのある場所から実行します。

```sh
mvn clean test
mvn -q compile exec:java
```

採用版はApache Commons CSV1.14.1、Commons IO2.20.0、推移依存Commons Codec1.19.0、JUnit5.12.2です。JDK17.0.8/Maven3.9.16で確認。今回、通常のMaven Centralからこれらの不足依存を取得して原本4件成功、その後はofflinecacheで展開ZIPを実行しています。Maven本体や全プラグインもない完全な空cache環境の検証とは区別します。最新版という意味ではなく、この実行で固定した版です。

## 入力はヘッダーなしUTF-8 CSV

```text
123,SKU-001
456,SKU-002
```

先頭にUTF-8 BOMの実バイトEF BB BFを付け、Commons CSVで読みます。1列目が数量、2列目がSKUです。1行目はヘッダーではなくデータなので、ヘッダーとして読み飛ばしません。メモリ内に正確なバイト列を作り、実際のInputStream/UTF-8 Reader/Commons CSVの経路を通します。独自のCSV分割実装ではありません。

そのままデコードすると最初の数量はU+FEFFを含む文字列になります。`trim()`も`strip()`もこのU+FEFFを消さず、Integer.parseIntが失敗します。同じCSVの2行目456は成功します。数字そのものの不正と、ストリーム先頭のBOMを分けて確認します。

## BOM処理はデコーダーより前へ置く

`Utf8BomCsv.parseRaw(byte[])`は未処理、`parseWithUtf8BomBoundary(byte[])`はBOMInputStreamにUTF_8だけを設定しinclude=falseで先頭BOMを除いてから同じUTF-8 ReaderとCSV parserへ渡します。両方ともCommons CSV DEFAULTをヘッダー指定なしで使います。

BOMなしの同じCSVは変更せず読めます。2行目の数量にU+FEFFが入っていれば、それはストリーム先頭のBOMではないので残します。データ中のU+FEFFを一括replaceして不正な数量を受け入れる修正は行いません。この例はUTF-8入力契約で、UTF-16等の自動判定・変換をしません。既存の厳密な数量パーサーやBOMヘッダー拒否の別サンプルを置き換えるものではありません。

## コードと4テスト

- `Utf8BomCsv.java`: 入力境界のBefore/Afterと、Commons CSVで取得するRow。
- `BomCsvDemo.java`: 正確な3つのバイトfixtureと、先頭hex・フィールドのcodepoint・変換結果を出すmain。
- `Utf8BomCsvTest.java`: raw先頭BOMとtrim/strip/NFE、境界処理後の123/456、BOMなし、途中FEFFの保持/拒否の4件。SKUも保持することをassertします。

```text
INPUT prefix=EF BB BF 31 32 33
RAW row=1 codePoints=U+FEFF,U+0031,U+0032,U+0033 trimSame=true stripSame=true number=NumberFormatException
RAW row=2 codePoints=U+0034,U+0035,U+0036 number=456
FIXED numbers=[123, 456]
NO_BOM numbers=[123, 456]
EMBEDDED row=2 codePoints=U+FEFF,U+0034,U+0035,U+0036 number=NumberFormatException
```

実行時の例外メッセージ全文には依存しません。不一致をassertThrowsで捕まえるケースもJUnit結果は成功です。独立故障コピーでBOMInputStreamのincludeをtrueにすると先頭BOMが残り、修正側のテストが失敗します。復元後は4件成功します。故障コピー・target・依存jar・ログはZIPへ含めず、MIT/POM/README/対象2ファイル/Testの6ファイルだけを収録します。外部ファイル・CSVシステム・DB/APIへの接続はなく、UTF-8の破損バイトや一般のCSV入力検証は範囲外です。

[Apache Commons CSV公式のBOM処理ガイド](https://commons.apache.org/proper/commons-csv/apidocs/index.html#handling-byte-order-marks)はBOMInputStreamをUTF-8 Readerの前へ置く構成を説明します。サイトの開発版API表示と、上で実行した固定依存版は区別しています。
