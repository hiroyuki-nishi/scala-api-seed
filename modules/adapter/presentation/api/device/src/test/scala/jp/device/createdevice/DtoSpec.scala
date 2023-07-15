package jp.device.createdevice

import io.circe.jawn.decode
import org.scalamock.matchers.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.diagrams.Diagrams
import org.scalatest.wordspec.AnyWordSpec

class DtoSpec extends AnyWordSpec with GivenWhenThen with Matchers with MockFactory with Diagrams {

  "JsonProtocol" when {
    "正常なリクエストが送信された時" should {
      "200が返される" in new WithFixture {
        When("処理実行")
        private val json =
          """{
            | "company_id": "COMPANY_A",
            | "device_name": "DEVICE_A"
            |}""".stripMargin

        Then("200 OKが返される")
        decode[CreateDeviceRequest](json) match {
          case Right(person) => println(person)
          case Left(error)   => println(error)
        }
      }
    }

    // TODO: nishi パースエラーなどのケースを追加
  }

  trait WithFixture {}
}
