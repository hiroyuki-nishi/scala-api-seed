# MySQLのコンテナに入る方法

```
1. コンテナのIDを調べる
docker ps

2. コンテナに入る
docker exec -it {コンテナID} bash

例) 
docker exec -it f5c91af3d25c bash

3. コンテナからmysqlに入る
mysql -u root -p

4. データベース一覧確認
show databases;

5. データベース切替
use {データベース名}

例) 
use test_database
```