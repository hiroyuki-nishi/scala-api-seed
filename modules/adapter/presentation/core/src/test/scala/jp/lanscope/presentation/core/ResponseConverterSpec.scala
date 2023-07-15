package jp.lanscope.presentation.core

import org.scalamock.matchers.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.diagrams.Diagrams
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import jp.lanscope.presentation.core.ResponseConverter.ApiResponse

class ResponseConverterSpec extends AnyWordSpec with GivenWhenThen with Matchers with MockFactory with Diagrams {

  "ResponseConverter" when {
    "Right(値あり)が渡された時" should {
      "statusCode: 200が返されること" in new WithFixture {
        When("処理実行")
        private val actual = Right("TEST").toResponse
        println(actual)
        Then("200 OKが返される")
        actual.getStatusCode should be(200)
        actual.getBody should be(""""TEST"""")
      }
    }

    "Right(空)が渡された時" should {
      "statusCode: 204が返されること" in new WithFixture {
        When("処理実行")
        private val actual = Right(()).toResponse
        println(actual)
        Then("204 が返される")
        actual.getStatusCode should be(204)
      }
    }
  }

  trait WithFixture {}
}
