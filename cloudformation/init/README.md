# AWSアカウントを最初に作成した際に環境構築するためのCloudformation
## 事前準備
## 構築手順
1. initialize.yamlをAWSコンソール上で実行してリソースを作成します。
2. developer-user-policyをPowerUserに付与する。
 
### 作成されるもの
#### IAM Role
* 全体インフラ作成用のRole
* CodePipeline用のRole
* CodeBuild用のRole
* APIGatewayでログを出力するためのRole
* VPC+Subnet+SecurityGroup
* IAMユーザーに許可する開発者ポリシー(CloudFormationからPassRole許可など） (#TODO)

#### KMS 
* Codepipeline用のKMS

#### S3
* samデプロイ用のS3
* CloudFrontLog用のS3
* Code系のArtifactsのS3

#### Route53(Https通信用のHostZone)

2. iam.yamlをAWSコンソール上で実行してリソースを作成します。(マルチアカウント環境の場合、必要なアカウントで一度流せばOKです。)
### 作成されるもの
#### IAM Role
* CodeCommit用のRole


3. 独自ドメインの取得をするために、 freenomなどで独自ドメインを取得します。
http://www.freenom.com/ja/index.html

3-1. 取得したドメインの「Management Tools」画面のNameServerからRoute53に登録した HostZoneのネームサーバーを2つ入力します。
例：xxxx.awsdns-30.org
   yyyyy.awsdns-24.net


4. Certificate Managerで証明書を発行
前提：
4-1. freenomでドメインを取得する際のネームサーバが必要なのでRoute53の HostZone作成が必要
4-2. freenomでドメインを取得したら、CertificateMangerで
     ルートドメインとサブドメインに対して証明書をリクエストしたいので、ドメイン名を追加 
　　例: nishiemon.tk
       *.nishiemon.tk
4-3. 「Route53でのレコード作成」ボタンをクリックして、Route53にCSMのレコードを追加します。 
4-4. 「状況」列が「発行済み」になっていることを確認します。
4-5. Route53に「CNAME」でCSMの新しいレコードが作成されていることを確認します。


### カスタムドメインの期限が切れた場合(証明書の新規作成)
1. 新しいドメインをfreenomから取得する
https://my.freenom.com/clientarea.php?action=domaindetails&id=1118988786&modop=custom&a=upgradef2p

2. 取得したドメインの「Management Tools」画面のNameServerからRoute53に登録した HostZoneのネームサーバーを2つ入力します。
例：xxxx.awsdns-30.org
yyyyy.awsdns-24.net

3. us-east-1, ap-northeast-1で手順1で取得したルートドメインとサブドメインに対して証明書をリクエストしたいので、ドメイン名を追加
例: nishiemon.tk
*.nishiemon.tk

4. cloudfront-spa.yamlとsample-codepipline.yaml(SPAとAPIの証明書)のCSMのarnを手順3で発行したarnに更新してpush
5. CodePipeline: sample-grayを実行して更新
https://ap-northeast-1.console.aws.amazon.com/codesuite/codepipeline/pipelines/sample-gray/view?region=ap-northeast-1#

-------------

疎通確認用のcurlコマンド例
**GET**
curl https://nishiemon.tk/v1/device
**POST**
curl -d '{"company_id": "A", "account_id": "A", "person_id": "A"}' -H "Content-Type: application/json" -X POST https://nishiemon.tk/v1/sample

