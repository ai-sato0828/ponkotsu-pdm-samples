# Pageableの入力境界をDB検索前に確認する（2289）

JDK17、Maven3.9系。Spring Boot3.5.16 / Spring Framework6.2.19 / Spring Data Commons3.5.13 / Hibernate6.6.53.Final / H2 2.3.232 / JUnit5.12.2。

```sh
mvn test
mvn package
java -jar target/pageable-input-boundary-2289-1.0.0.jar
```

最初の実行では依存取得のネットワークが必要です。検証はJava17.0.8/Maven3.9.16の既存キャッシュでoffline実行しています。完全に空のキャッシュからの取得は未確認です。

## APIとデータ

`GET /inquiries?page=0&size=2`。pageは0始まり、既定0/20。sizeは1..50。createdAt DESC、id DESC。同時刻のOPEN3件（ID1,2,3）とCLOSED1件（ID4）だけを、起動時のメモリH2へ入れます。GETは更新しません。外部DB/API・認証は使いません。

```json
{"items":[{"id":3,"title":"問い合わせ3"},{"id":2,"title":"問い合わせ2"}],"page":0,"size":2,"totalElements":3,"totalPages":2,"hasNext":true}
```

2ページ目(page=1)はID1だけ、page=2は200で空配列・totalElements=3・hasNext=falseです。範囲外だから400にする仕様ではありません。

## 入力ガードと巨大境界

`page<0 || size<1 || size>50 || (long) page*size>Integer.MAX_VALUE`をRepository呼出し前に確認し、違反は400。pageとsizeがintでも積がintを超えるので、掛け算の前にlongへ変換します。`(long)(page*size)`はintでオーバーフローした後の変換になり、このガードには使えません。

| page | size | long offset | 結果 |
|---|---|---|---|
| 42949672 | 50 | 2147483600 | 200・空・total3・hasNext=false |
| 42949673 | 50 | 2147483650 | 400・SQL準備0 |
| 2147483647 | 1 | 2147483647 | 200・空・total3・hasNext=false |
| 2147483647 | 50 | 107374182350 | 400・SQL準備0 |

採用版のPageImpl.hasNextはintのpage+1比較を使うため、最大pageでtrueへ反転することを初回試験で確認しました。このJSONでは`(long) page + 1 < result.getTotalPages()`を使い、通常の中間ページtrue/最終ページfalseと巨大範囲外falseを保ちます。依存ライブラリを変更していません。

## テストの範囲

元のInquiryControllerTest2件は内容を変えず収録し、同時刻ID降順・最終/範囲外・size51を確認します。InquiryBoundaryTest13件は、負page/size0/51/積の上限超過5件、intに変換できない2件、許容する巨大offset2件、size1/50の2件、既定値1件、空DB1件を確認します。旧2件と合わせて15件です。テストは小さな固定データだけです。

拒否を確認する試験では、seedを完了してからHibernate Statistics.clear()、リクエスト、prepareStatementCount=0を確認します。これは当該リクエストがSQLを準備しないという意味で、起動/seedまでDBを使わないわけではありません。Mockitoへ置き換えていません。

```sh
python3 scripts/verify_http.py target/pageable-input-boundary-2289-1.0.0.jar
```

Python3の実HTTP確認は、127.0.0.1の空きポートでJARを起動し、既定/通常範囲外/巨大境界の200・400とJSONを見て終了します。別途JARを起動しておく必要はありません。スクリプトの--baselineは旧固定JARを1件だけ比較する検証用で、通常の修正版確認では指定しません。

## SQLで途中追加のページずれを確認する

`sql/offset-shift.sql`は記事本文のSQLと同じものです。JUnitの静的ページ境界とは別に、途中で新しい行が増えたときのoffsetとkeysetを比較します。空のH2メモリDBで1回だけ実行し、最初104/103、途中追加後offsetの次ページ103/102、keysetの次ページ102/101を確認します。

```sh
java -cp "$HOME/.m2/repository/com/h2database/h2/2.3.232/h2-2.3.232.jar" org.h2.tools.Shell -url 'jdbc:h2:mem:page-shift' -user sa -sql "$(cat sql/offset-shift.sql)"
```

Shellを終了するとメモリDBは消えます。このSQLはJPAのInquiryテーブルを操作しません。少数データで正しい値を観測したもので、数千万件のデータ生成・深いoffsetの性能測定・運用DBでの性能保証を行ったわけではありません。

## 既存配布との関係

旧spring/audit-behaviorの共有モジュールから必要な型と元の2テストだけを抽出。Entity/Repository/Statusと既存2テストはbyte不変。Controllerの変更はoffsetガードとhasNextの安全な算出だけです。Spring Web/JPA/H2の実構成を保持します。旧共有モジュール・その固定リンク・他記事用のコードは変更しません。

ZIPはソース・設定・JUnit・SQL・手順・HTTP確認・LICENSEを収録し、依存JAR、class、実行JAR、個人設定は含みません。
