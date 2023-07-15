package jp.lanscope.domain

import jp.lanscope.domain.DateTimeUtil.LocalDateSyntax
import org.scalamock.matchers.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.diagrams.Diagrams
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

import java.time.{LocalDate, ZoneId, ZonedDateTime}

class DateTimeUtilSpec extends AnyWordSpec with GivenWhenThen with Matchers with MockFactory with Diagrams {
  "toISOString(date: ZonedDateTime)" when {
    "1/1のZonedDateTimeを変換した時" should {
      "ISO-8601 (RFC3339)` 形式の日時文字列になっていること" in new WithFixture {
        When("処理実行")
        private val actual = DateTimeUtil.toISOString(ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")))
        println(actual)

        Then("ISO-8601 (RFC3339)` 形式の日時文字列になっている")
        actual should be("2021-01-01T00:00:00.000Z")
      }
    }

    "12/31のZonedDateTimeを変換した時" should {
      "ISO-8601 (RFC3339)` 形式の日時文字列になっていること" in new WithFixture {
        When("処理実行")
        private val actual = DateTimeUtil.toISOString(ZonedDateTime.of(2021, 12, 31, 23, 59, 59, 999, ZoneId.of("UTC")))
        println(actual)

        Then("ISO-8601 (RFC3339)` 形式の日時文字列になっている")
        actual should be("2021-12-31T23:59:59.000Z")
      }
    }
  }

  "toISOString(date: LocalDate)" when {
    "2021/01/01のLocalDateを変換した時" should {
      "yyyyMMdd形式の文字列になっていること" in new WithFixture {
        When("処理実行")
        private val actual = DateTimeUtil.toISOString(LocalDate.of(2021, 1, 1))
        println(actual)

        Then("yyyyMMddの文字列になっている")
        actual should be("20210101")
      }
    }

    "2021/12/31のLocalDateを変換した時" should {
      "yyyyMMdd形式の文字列になっていること" in new WithFixture {
        When("処理実行")
        private val actual = DateTimeUtil.toISOString(LocalDate.of(2021, 12, 31))
        println(actual)

        Then("yyyyMMddの文字列になっている")
        actual should be("20211231")
      }
    }
  }

  "toISOMinutesString(date: ZonedDateTime)" when {
    "2021/01/01のZonedDateTimeを変換した時" should {
      "yyyy-MM-dd'T'HH:mm形式の文字列になっていること" in new WithFixture {
        When("処理実行")
        private val actual = DateTimeUtil.toISOMinutesString(ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")))
        println(actual)

        Then("yyyyMMddの文字列になっている")
        actual should be("2021-01-01T00:00")
      }
    }

    "2021/12/31のZonedDateTimeを変換した時" should {
      "yyyy-MM-dd'T'HH:mm形式の文字列になっていること" in new WithFixture {
        When("処理実行")
        private val actual = DateTimeUtil.toISOMinutesString(ZonedDateTime.of(2021, 12, 31, 23, 59, 59, 999, ZoneId.of("UTC")))
        println(actual)

        Then("yyyyMMddの文字列になっている")
        actual should be("2021-12-31T23:59")
      }
    }
  }

  "fromISOString(date: String)" when {
    "2021-01-01T00:00:00.000Zの文字列を変換した時" should {
      "ZonedDateTimeに変換できること" in new WithFixture {
        When("処理実行")
        private val actual = DateTimeUtil.fromISOString("2021-01-01T00:00:00.000Z")
        println(actual)

        Then("変換成功")
        actual should be(ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")))
      }
    }

    "2021-12-31T23:59:59.000Zの文字列を変換した時" should {
      "ZonedDateTimeに変換できること" in new WithFixture {
        When("処理実行")
        private val actual = DateTimeUtil.fromISOString("2021-12-31T23:59:59.000Z")
        println(actual)

        Then("変換成功")
        actual should be(ZonedDateTime.of(2021, 12, 31, 23, 59, 59, 0, ZoneId.of("UTC")))
      }
    }
  }

  "toInstant(date: ZonedDateTime)" when {
    "ZonedDateTimeを変換した時" should {
      "Instantに変換できること" in new WithFixture {
        When("処理実行")
        private val actual = DateTimeUtil.toInstant(ZonedDateTime.of(2021, 3, 21, 23, 59, 59, 0, ZoneId.of("UTC")))
        println(actual)

        Then("変換成功")
      }
    }
  }

  "<=" when {
    "a < b" should {
      "trueになる" in new WithFixture {
        When("処理実行")
        private val actual: Boolean = LocalDate.of(2021, 2, 3) <= LocalDate.of(2021, 3, 4)

        Then("trueになる")
        actual should be(true)
      }
    }
    "a = b" should {
      "trueになる" in new WithFixture {
        When("処理実行")
        private val actual = LocalDate.of(2021, 2, 3) <= LocalDate.of(2021, 2, 3)

        Then("trueになる")
        actual should be(true)
      }
    }
    "a > b" should {
      "falseになる" in new WithFixture {
        When("処理実行")
        private val actual = LocalDate.of(2021, 2, 3) <= LocalDate.of(2021, 1, 2)

        Then("falseになる")
        actual should be(false)
      }
    }
  }

  trait WithFixture {}
}
