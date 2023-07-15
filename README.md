# scala-seed

## 概要
- Scalaで機能開発をする際にテンプレートとなるリポジトリです。 Amazon Corretto 11 で動作します。

## コンパイルやテスト実施に必要なもの
* [JDK](https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html)
  (Java のプログラムの開発や実行を行うためのプログラムのセット。 JVM+JRE+コンパイラ＋デバッガ)
* [sbt](https://www.scala-sbt.org/1.x/docs/ja/Setup.html)
  (Scala用のビルドツール。SDKMANなどで複数のsbtバージョンを切り替えられるようにしておくと便利です)
* [localstack](https://github.com/localstack/localstack)  
  (ローカルにAWSのモック/テスト環境を作ってくれてるツール)
  
## インストール方法
下記シェルスクリプトを実行して必要なツール群のインストールを行ってください。 
```
sh install.sh
```

※ IntelliJ IDEA の読み込みでJDKのエラーが発生する場合は下記のメニュー
[Project Settings]-[Project]-Project SDKにcorreto11を指定してください。


## ディレクトリ構成
オニオンアーキテクチャを元にディレクトリ構成を作成しております。
```
├── modules
│ ├── adapter
│ │ ├── infrastructure [インフラストラクチャ層]
│ │ └── presentation [プレゼンテーション+アプリケーション層]
│ │     ├── api [API処理]
│ │     ├── consumer [コンシューマー処理]
│ │     └── core [APIとコンシューマーで共通して利用するクラスなどの置場]
│ ├── domain [ドメイン層]
│ └── sender [リポジトリで永続化しないインフラストラクチャのインタフェースやパース処理などの置場]
├── project
│ ├── Dependencies.scala [ライブラリ定義]
│ ├── build.properties [sbtバージョンの設定]
│ └── plugins.sbt [sbtプラグインの設定]
│ 
├── build.sbt [sbtの設定]
├── buildspec_api.yml [APIのCodeBuildのビルド定義]
├── buildspec_consumer.yml [コンシューマーのCodeBuildのビルド定義]
├── docker-compose.yaml
├── README.md
├── sam_template_api.yml [APIのSAMデプロイ定義]
└── sam_template_consumer.yml [コンシューマーのSAMデプロイ定義]
```

## テスト
```
docker-compose up -d
```

