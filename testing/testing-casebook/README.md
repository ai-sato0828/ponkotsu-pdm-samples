# 仕様から作るテストとJUnitのパラメータ化

対応記事:
- https://syachiku-uz.info/how-to-create-test-cases-from-spec/
- https://syachiku-uz.info/junit-parameterizedtest-csvsource-boundary-values/

Java17、Maven3.9.5、JUnit5.12.2。Spring/DB/外部API不要。初回の依存ライブラリ取得にはネット接続が必要。

```sh
mvn test
mvn -Dtest=CaseDesignTest test
mvn -Dtest=TitleRuleParameterizedTest test
mvn -Dtest=CsvSourceParsingTest test
```

取得済みキャッシュなら`mvn -o test`で実行できる。テストクラスは並行実行や実行順へ依存しない。

## ケースとテストの対応

仕様は[docs/spec.md](docs/spec.md)、手書きの期待値は[docs/cases.csv](docs/cases.csv)。CaseDesignTestに普通の@Testで20件を配置。TitleRuleParameterizedTestはそのうち件名のケースをパラメータ化して再確認する14回。CsvSourceParsingTestはCSVの引数変換5回と前後空白設定2回を確認する7回。合計41回で、41種類の業務入力という意味ではない。

| ケース | 仕様 | 入力・条件 | 期待値 | 普通のテスト |
|---|---|---|---|---|
| T01 | TITLE-01 | null | false | CaseDesignTest.T01_title |
| T02 | TITLE-01 | 空文字 | false | CaseDesignTest.T02_title |
| T03 | TITLE-02 | a | true | CaseDesignTest.T03_title |
| T04 | TITLE-02 | aa | true | CaseDesignTest.T04_title |
| T05 | TITLE-02 | a×99 | true | CaseDesignTest.T05_title |
| T06 | TITLE-02 | a×100 | true | CaseDesignTest.T06_title |
| T07 | TITLE-02 | a×101 | false | CaseDesignTest.T07_title |
| T08 | TITLE-01 | 半角空白1文字 | false | CaseDesignTest.T08_title |
| T09 | TITLE-01 | 全角空白1文字 | false | CaseDesignTest.T09_title |
| T10 | TITLE-02 | 😀×100 | true | CaseDesignTest.T10_title |
| T11 | TITLE-02 | 😀×101 | false | CaseDesignTest.T11_title |
| T12 | TITLE-03 | a×99＋半角空白 | true | CaseDesignTest.T12_title |
| T13 | TITLE-03 | a×100＋半角空白 | false | CaseDesignTest.T13_title |
| A01 | START-01 | RECEIVED, 担当20, 操作20 | true, IN_PROGRESS | CaseDesignTest.A01_start |
| A02 | START-02 | RECEIVED, 担当20, 操作10 | false, RECEIVED | CaseDesignTest.A02_start |
| A03 | START-02 | RECEIVED, 担当20, 操作30 | false, RECEIVED | CaseDesignTest.A03_start |
| A04 | START-02 | RECEIVED, 担当null（未設定）, 操作20 | false, RECEIVED | CaseDesignTest.A04_start |
| A05 | START-02 | DRAFT, 担当20, 操作20 | false, DRAFT | CaseDesignTest.A05_start |
| A06 | START-02 | IN_PROGRESS, 担当20, 操作20 | false, IN_PROGRESS | CaseDesignTest.A06_start |
| A07 | START-02 | COMPLETED, 担当20, 操作20 | false, COMPLETED | CaseDesignTest.A07_start |

CsvSource境界6行はT02〜T07。NullAndEmptySourceはT01/T02、ValueSourceはT08/T09、MethodSourceはT10〜T13。CSV変換の5行と前後空白設定2件は業務ケースとは別で、引用なし空欄はnull、''は空文字、' 'は空白、'a,b'はカンマ入り文字列、NULLはnullValues未指定なので文字列です。引用していない前後空白は既定で除去されます。ignoreLeadingAndTrailingWhitespace=falseなら保持されます。

## ファイル

- TitleRule.java: 件名のコードポイント数と未入力を判定。
- Inquiry.java: 担当者と状態から開始を判定。
- CaseDesignTest.java: 20件の仕様ケースを普通の@Testで示す。
- TitleRuleParameterizedTest.java: 同じ件名仕様のテストをパラメータ化。
- CsvSourceParsingTest.java: CsvSourceの値変換を確認。

配布ZIPはこのプロジェクトのpom/src/docs/説明/ライセンスを含み、target・依存jar・ZIP自身は含まない。独自コードはMITライセンス。実行結果と変更を検出した確認はVERIFICATION.mdへ記録。
