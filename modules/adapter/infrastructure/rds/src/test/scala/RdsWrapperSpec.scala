import org.scalatest.wordspec.AnyWordSpec
import scalikejdbc._

import java.sql.DriverManager
import scala.util.{Failure, Success, Try}

class RdsWrapperSpec extends AnyWordSpec {
  "テスト" when {
    "ConnectionPoolのaddでテーブルを作成(省略形）" should {
      "作成できる" in new WithFixture {
        Class.forName("com.mysql.jdbc.Driver")
        ConnectionPool.add(Symbol("hoge"), "jdbc:mysql://localhost:3306/test_database", "root", "root")
        NamedDB(Symbol("hoge")) localTx { implicit session =>
          hoge("hoge")
        }
      }
    }

    "Connectionプールでテーブル作成" should {
      "作成できる" in new WithFixture {
        Class.forName("com.mysql.jdbc.Driver")
        ConnectionPool.add(Symbol("hoge"), "jdbc:mysql://localhost:3306/test_database", "root", "root")
        using(DB(ConnectionPool(Symbol("hoge")).borrow())) { db =>
          db localTx { implicit session =>
            hoge("piyo")
          }
        }
      }
    }

    "driverManagerテーブルを作成(driverManager)" should {
      "作成できる" in new WithFixture {
        Class.forName("com.mysql.jdbc.Driver")
        using(DB(DriverManager.getConnection("jdbc:mysql://localhost:3306/test_database", "root", "root"))) { db =>
          db localTx { implicit session =>
            hoge("driverManager")
          }
        }
      }
    }

    "ConnectionPoolでテーブルを作成(driverManager)" should {
      "作成できる" in new WithFixture {
        Class.forName("com.mysql.jdbc.Driver")
        ConnectionPool.singleton("jdbc:mysql://localhost:3306/test_database", "root", "root")
        using(DB(ConnectionPool.borrow())) { db =>
          db localTx { implicit session =>
            hoge("connection_pool")
          }
        }
      }
    }

    /**
     * クエリ
     */
    "クエリをリードオンリーモードで実行" should {
      "実行できる" in new WithFixture {
        case class Foo(hello: String)
        val * = (rs: WrappedResultSet) => Foo(rs.string("hello"))

        Class.forName("com.mysql.jdbc.Driver")
        ConnectionPool.add(Symbol("hoge"), "jdbc:mysql://localhost:3306/test_database", "root", "root")
        NamedDB(Symbol("hoge")) readOnly { implicit session =>
          // Query
          val all = SQL("select * from foo").map(*).list().apply()
          print(all)
        }
      }
    }

    "クエリをautoCommitで実行" should {
      "実行できる" in new WithFixture {
        Class.forName("com.mysql.jdbc.Driver")
        ConnectionPool.add(Symbol("hoge"), "jdbc:mysql://localhost:3306/test_database", "root", "root")
        NamedDB(Symbol("hoge")) autoCommit { implicit session =>
          SQL("create table IF NOT EXISTS company (id integer primary key, name varchar(30), )").execute().apply()
        }
      }
    }

    "クエリをlocalTxで実行" should {
      "実行できる" in new WithFixture {
        Class.forName("com.mysql.jdbc.Driver")
        ConnectionPool.add(Symbol("hoge"), "jdbc:mysql://localhost:3306/test_database", "root", "root")
        NamedDB(Symbol("hoge")) localTx { implicit session =>
          SQL("insert into company values (?, ?)").bind(1,"Typesafe").update().apply()
        }
      }
    }

    "クエリをwithinTxで実行" should {
      "実行できる" in new WithFixture {
        Class.forName("com.mysql.jdbc.Driver")
        ConnectionPool.add(Symbol("hoge"), "jdbc:mysql://localhost:3306/test_database", "root", "root")
        myWithinTx(DB(ConnectionPool(Symbol("hoge")).borrow())) { implicit session =>
          SQL("insert into company values (?, ?)").bind(4, "Typesafe").update().apply()
        }
      }
    }

    /**
     * クエリAPI(single, list, foreach)
     */

    "クエリをsingleで実行" should {
      "実行できる" in new WithFixture {
        case class Company(id: String, name: String)
        val companyMapper = (rs: WrappedResultSet) => Company(rs.string("id"), rs.string("name"))

        Class.forName("com.mysql.jdbc.Driver")
        ConnectionPool.add(Symbol("hoge"), "jdbc:mysql://localhost:3306/test_database", "root", "root")
        val result = NamedDB(Symbol("hoge")) readOnly { implicit session =>
          SQL("select * from company where id = ?").bind(1).map(rs => Company(rs.string("id"), rs.string("name"))).single().apply()
        }
        println(result)

        // mapperを定義したケース
        val result2 = NamedDB(Symbol("hoge")) readOnly { implicit session =>
          SQL("select * from company where id = ?").bind(1).map(companyMapper).single().apply()
        }
        println(result2)
      }
    }

    "クエリをfirstで実行" should {
      "実行できる" in new WithFixture {
        case class Company(id: String, name: String)

        Class.forName("com.mysql.jdbc.Driver")
        ConnectionPool.add(Symbol("hoge"), "jdbc:mysql://localhost:3306/test_database", "root", "root")
        val result = NamedDB(Symbol("hoge")) readOnly { implicit session =>
          SQL("select * from company").map(rs => Company(rs.string("id"), rs.string("name"))).first().apply()
        }
        println(result)
      }
    }

    "クエリをlistで実行" should {
      "実行できる" in new WithFixture {
        case class Company(id: String, name: String)

        Class.forName("com.mysql.jdbc.Driver")
        ConnectionPool.add(Symbol("hoge"), "jdbc:mysql://localhost:3306/test_database", "root", "root")
        val result = NamedDB(Symbol("hoge")) readOnly { implicit session =>
          SQL("select * from company").map(rs => Company(rs.string("id"), rs.string("name"))).list().apply()
        }
        println(result)
      }
    }

    "クエリをforeachで実行" should {
      "実行できる" in new WithFixture {
        case class Company(id: String, name: String)

        Class.forName("com.mysql.jdbc.Driver")
        ConnectionPool.add(Symbol("hoge"), "jdbc:mysql://localhost:3306/test_database", "root", "root")
        NamedDB(Symbol("hoge")) readOnly { implicit session =>
          SQL("select * from company").foreach(rs => println(rs.string("id"), rs.string("name")))
        }
      }
    }

    /**
     * Update API
     */

//    "クエリをlocalTxで実行" should {
//      "実行できる" in new WithFixture {
//        Class.forName("com.mysql.jdbc.Driver")
//        ConnectionPool.add(Symbol("hoge"), "jdbc:mysql://localhost:3306/test_database", "root", "root")
//        NamedDB(Symbol("hoge")) localTx { implicit session =>
//          SQL("insert into company values (?, ?)").bind(1,"Typesafe").update().apply()
//        }
//      }
//    }

    def myWithinTx[A](db: DB)(f: DBSession => A): A = {
      using(db) { db =>
        try {
          db.begin()
          val result = db withinTx { implicit session =>
            f(session)
          }
          result match {
            case Failure(_) => db.rollback()
            case Left(_)    => db.rollback()
            case _          => db.commit()
          }
          result
        } catch {
          case e: Throwable =>
            println(e)
            db.rollback()
            throw e
        } finally {
          db.close()
        }
      }
    }

    def hoge(tableName: String)(implicit session: DBSession): Unit = {
      (for {
        _ <- createTable(tableName)
      } yield ()) match {
        case Success(_) =>
          session.connection.commit()
          println("success")
        case Failure(e) =>
          session.connection.rollback()
          println(e)
      }
    }

    def createTable(tableName: String)(implicit session: DBSession): Try[Boolean] = Try {
      SQL(
        s"""
           |CREATE TABLE IF NOT EXISTS $tableName (hello varchar(100))
          """.stripMargin
      ).execute().apply()
    }
  }

  trait WithFixture { }
}
