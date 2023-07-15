import scalikejdbc._
import scalikejdbc.config.DBs
import transaction.{SessionHolder, TransactionAware}

case class LoginInfo(userName: String,  password: String)

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements", "org.wartremover.warts.Throw"))
trait ScalikeJdbcTransactionAware extends TransactionAware {
  protected val envName: String
  protected val loginInfo: LoginInfo
  protected val url: String
  protected val driver: String
  private val DbNames: Seq[String] = Seq("default", "readonly")

  protected def withDB[R](f: => R) = {
    DBs.setupAll()

    GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(
      enabled = true,
      singleLineMode = true,
      logLevel = Symbol("debug"),
      warningEnabled = true,
      warningThresholdMillis = 1000,
      warningLogLevel = Symbol("debug")
    )

    DbNames.foreach(name => {
      Class.forName(driver)
      ConnectionPool.add(Symbol(name), url, loginInfo.userName, loginInfo.password)
    })

    try {
      f
    } finally {
      DBs.closeAll()
    }
  }

  def defaultDB: DB = DB(ConnectionPool.borrow()) //NamedDB使いたい場合は、NamedDB('hogehoge).toDB とかにする感じ？

  /**
    * トランザクション境界を定義します。
    * このメソッドは、[[TransactionAware#txLocal]]に対するエイリアスです。
    *
    * @param f 同じトランザクションで行う処理
    * @return パラメータに指定された処理の結果
    */
  def tx[A](f: SessionHolder => A): A = tx(defaultDB)(f)

  /**
    * トランザクション境界を定義します。
    * このメソッドは、[[TransactionAware#txLocal]]に対するエイリアスです。
    *
    * @param f 同じトランザクションで行う処理
    * @return パラメータに指定された処理の結果
    */
  def tx[A](db: DB)(f: SessionHolder => A): A = txLocal(db)(f)

  /**
    * トランザクション境界を定義します。
    * このブロック内で記述された処理は、SessionHolderを使用して、データ
    * ベースにアクセスる処理を記述することが出来ます。ブロック内の処理は、
    * 全て同じ DBSession 、同じトランザクションで処理されます。
    * 処理に成功した場合はトランザクションはコミットされます。
    * 以下の場合は、トランザクションはロールバックされます。
    *
    * - ブロックの処理結果がscala.util.Failureであった場合。
    * - ブロックの処理結果がLeftであった場合。
    * - ブロックの処理中に例外が投げられた場合。
    *
    * このブロックをネストして定義した場合は、それぞれ異なるトランザ
    * クションが発生します。(トランザクションのマージは行われません。)
    *
    * @param db
    * @param f 同じトランザクションで行う処理
    * @tparam A
    * @return パラメータに指定された処理の結果
    */
  def txLocal[A](db: DB)(f: SessionHolder => A): A = {
    def commit(db: DB) = {
      db.commit()
    }
    def rollback(db: DB) = {
      db.rollbackIfActive()
    }

    using(db) { db =>
      try {
        db.begin()

        /**
         * クエリや更新を既に存在しているトランザクション内で実行します。
         * トランザクションについての操作（Tx#begin()、 Tx#rollback() や Tx#commit()）は
         * すべてライブラリ利用者によって制御される必要があります。
         */
        val result = db withinTx { implicit session =>
          f(SessionHolder(session))
        }
        result match {
          case util.Failure(_) => rollback(db)
          case Left(_)         => rollback(db)
          case _               => commit(db)
        }
        result
      } catch {
        case e: Exception =>
          rollback(db)
          throw e
      }
    }
  }

  /**
   * Reusing same DB instance several times
   * http://scalikejdbc.org/documentation/connection-pool.html
   */
  def txReadOnly[A](f: SessionHolder => A): A =
    defaultDB readOnly { session =>
      f(SessionHolder(session))
    }
}
