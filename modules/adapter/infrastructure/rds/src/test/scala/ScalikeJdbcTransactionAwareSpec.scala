import org.scalamock.scalatest.MockFactory
import org.scalatest.diagrams.Diagrams
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen}
import scalikejdbc._
import scalikejdbc.config.DBs
import transaction.SessionAware

import scala.util.{Failure, Success, Try}

class ScalikeJdbcTransactionAwareSpec
    extends AnyWordSpec
    with GivenWhenThen
    with Matchers
    with MockFactory
    with Diagrams
    with BeforeAndAfterAll {
  case class Dummy(
      id: String,
      no: Long,
      name: String
  )

  /**
   * 簡単なORM
   */
  object Dummy extends SQLSyntaxSupport[Dummy] {
    override val tableName = "dummy"
    def apply(d: ResultName[Dummy])(rs: WrappedResultSet): Dummy =
      new Dummy(
        id = rs.get(d.id),
        no = rs.get(d.no),
        name = rs.get(d.name)
      )
  }

  override def beforeAll(): Unit = {
    val dummyColumn = Dummy.column
    new WithFixture {
      withDB {
        tx { implicit session =>
          sql"""
            CREATE TABLE IF NOT EXISTS dummy (
              id varchar(32) NOT NULL,
              no bigint NOT NULL,
              name text NOT NULL,
              PRIMARY KEY ( id )
            )
           """.execute().apply()
        }
      }
    }
    new WithFixture {
      // TODO: nishi
      withDB {
        // TODO: nishi
        tx { implicit session =>
          // TODO: nishi
          withSQL {
            insert
              .into(Dummy)
              .namedValues(
                dummyColumn.id   -> "TESTID",
                dummyColumn.no   -> 123,
                dummyColumn.name -> "TESTNAME"
              )
          }.update().apply() // TODO: nishi update?
        }
      }
    }
  }

  override def afterAll(): Unit = {
    new WithFixture {
      withDB {
        tx { implicit session =>
          withSQL {
            // TODO: nishi どうやって削除している？
            delete.from(Dummy)
          }.update().apply()
        }
      }
    }
  }

  "dummy" when {
    "テスト" should {
      "ど正常" in new WithFixture {
        val d = Dummy.syntax("d")
        val actual = withDB {
          tx { implicit session =>
            withSQL {
              select.from(Dummy as d).where.eq(d.id, "TESTID")
            }.map(Dummy(d.resultName)).single().apply()
          }
        }
        actual should be(Some(Dummy("TESTID", 123, "TESTNAME")))
      }
    }

    "テスト" should {
      "土星城" in new WithFixture {
        DBs.setupAll()

        GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(
          enabled = true,
          singleLineMode = true,
          logLevel = Symbol("debug"),
          warningEnabled = true,
          warningThresholdMillis = 1000,
          warningLogLevel = Symbol("debug")
        )

        //
        Class.forName(driver)
        ConnectionPool.add(Symbol("hoge"), url, loginInfo.userName, loginInfo.password)
        NamedDB(Symbol("hoge")) localTx { implicit  session =>
//          NamedDB(ConnectionPool.borrow(Symbol("hoge"))) localTx {implicit  session =>
            (
              for {
                _ <- createTable()
                _ <- putData("fuga")
//                rs <- listTable()
              } yield ()) match {
              case Success(_) =>
                session.connection.commit()
                println("success")
              case Failure(e) =>
                session.connection.rollback()
                println(e)
            }
          }
        }

      def createTable()(implicit session: DBSession): Try[Boolean] = Try {
        SQL(
          """
            |CREATE TABLE IF NOT EXISTS foo (hello varchar(100))
      """.stripMargin
        ).execute().apply()
      }

      def putData(name: String)(implicit session: DBSession): Try[Boolean] = Try {
        SQL(
          s"""
            |insert into foo (hello) values ("$name")
      """.stripMargin
        ).execute().apply()

      }

//      def listTable()(implicit session: DBSession): Try[Boolean] = Try {
//        SQL(
//          """
//            |select * from foo
//      """.stripMargin
//        ).execute().apply()
//      }
      }
//        DbNames.foreach(name => {
//          Class.forName(driver)
//          ConnectionPool.add(Symbol(name), url, loginInfo.userName, loginInfo.password)
//        })
//
//        try {
//          f
//        } finally {
//          DBs.closeAll()
//        }
  }

  trait WithFixture extends ScalikeJdbcTransactionAware with SessionAware[DBSession] {
    override protected val envName = "local"
    override protected val loginInfo = LoginInfo(
//      readWriteUrl = "localhost:5432",
//      readOnlyUrl = "localhost:5432",
      userName = "root",
      password = "root"
    )
//    override protected val url: String    = "jdbc:postgresql://localhost:5432/report"
    override protected val url: String    = "jdbc:mysql://localhost:3306/test_database"
    override protected val driver: String = "com.mysql.jdbc.Driver"
//    override protected val driver: String = "org.postgresql.Driver"
  }
}
