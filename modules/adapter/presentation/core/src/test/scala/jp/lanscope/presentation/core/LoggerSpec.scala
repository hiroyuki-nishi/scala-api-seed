package jp.lanscope.presentation.core

import jp.lanscope.domain.{ClientId, CompanyId, DeviceId}
import org.scalamock.matchers.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.diagrams.Diagrams
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class LoggerSpec extends AnyWordSpec with GivenWhenThen with Matchers with MockFactory with Diagrams {

  "Logger" when {
    "Level.Infoのログを作成した時" should {
      "Level.Infoのログが作成できる" in new WithFixture {
        When("処理実行")
        private val actual = Logger(
          Level.Info,
          Some(CompanyId("CCCCCC")),
          Some(ClientId("DDDDD")),
          Some(DeviceId("DDDDD")),
          Some("XXXXXXXXX"),
          None,
          "任意のメッセージ",
          "スタックトレース"
        )

        Then("Log型が作れる")
        actual.level should be(Level.Info)
        actual.companyId should be(Some(CompanyId("CCCCCC")))
        actual.clientId should be(Some(ClientId("DDDDD")))
        actual.specifiedId should be(Some("XXXXXXXXX"))
        actual.message should be("任意のメッセージ")
        actual.trace should be("スタックトレース")
      }
    }

    "Level.Warnのログを作成した時" should {
      "Level.Warnのログが作成できる" in new WithFixture {
        When("処理実行")
        private val actual = Logger(
          Level.Warn,
          Some(CompanyId("CCCCCC")),
          Some(ClientId("DDDDD")),
          Some(DeviceId("DDDDD")),
          Some("XXXXXXXXX"),
          None,
          "任意のメッセージ",
          "スタックトレース"
        )

        Then("Log型が作れる")
        actual.level should be(Level.Warn)
        actual.companyId should be(Some(CompanyId("CCCCCC")))
        actual.clientId should be(Some(ClientId("DDDDD")))
        actual.specifiedId should be(Some("XXXXXXXXX"))
        actual.message should be("任意のメッセージ")
        actual.trace should be("スタックトレース")
      }

      "Level.Errorのログを作成した時" should {
        "Level.Errorのログが作成できる" in new WithFixture {
          When("処理実行")
          private val actual = Logger(
            Level.Error,
            Some(CompanyId("CCCCCC")),
            Some(ClientId("DDDDD")),
            Some(DeviceId("DDDDD")),
            Some("XXXXXXXXX"),
            None,
            "任意のメッセージ",
            "スタックトレース"
          )

          Then("Log型が作れる")
          actual.level should be(Level.Error)
          actual.companyId should be(Some(CompanyId("CCCCCC")))
          actual.clientId should be(Some(ClientId("DDDDD")))
          actual.specifiedId should be(Some("XXXXXXXXX"))
          actual.message should be("任意のメッセージ")
          actual.trace should be("スタックトレース")
        }
      }

      "Level.Debugのログを作成した時" should {
        "Level.Debugのログが作成できる" in new WithFixture {
          When("処理実行")
          private val actual = Logger(
            Level.Debug,
            Some(CompanyId("CCCCCC")),
            Some(ClientId("DDDDD")),
            Some(DeviceId("DDDDD")),
            Some("XXXXXXXXX"),
            None,
            "任意のメッセージ",
            "スタックトレース"
          )

          Then("Log型が作れる")
          actual.level should be(Level.Debug)
          actual.companyId should be(Some(CompanyId("CCCCCC")))
          actual.clientId should be(Some(ClientId("DDDDD")))
          actual.specifiedId should be(Some("XXXXXXXXX"))
          actual.message should be("任意のメッセージ")
          actual.trace should be("スタックトレース")
        }
      }

      "Level.Traceのログを作成した時" should {
        "Level.Traceのログが作成できる" in new WithFixture {
          When("処理実行")
          private val actual = Logger(
            Level.Trace,
            Some(CompanyId("CCCCCC")),
            Some(ClientId("DDDDD")),
            Some(DeviceId("DDDDD")),
            Some("XXXXXXXXX"),
            None,
            "任意のメッセージ",
            "スタックトレース"
          )

          Then("Log型が作れる")
          actual.level should be(Level.Trace)
          actual.companyId should be(Some(CompanyId("CCCCCC")))
          actual.clientId should be(Some(ClientId("DDDDD")))
          actual.specifiedId should be(Some("XXXXXXXXX"))
          actual.message should be("任意のメッセージ")
          actual.trace should be("スタックトレース")
        }
      }
    }
    trait WithFixture {}
  }
}
