# 構築手順
1. us-east1でnishiemon.tk, *.nishiemon.tkの証明書を発行する
2. cloudfront-spa.yamlのCSMのarnを1で作成したarnに置き換える
3. sample.yamlをCloudFormationから実行する