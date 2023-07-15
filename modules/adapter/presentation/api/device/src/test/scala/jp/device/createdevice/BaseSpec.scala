package jp.device.createdevice
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.ProxyRequestContext
import jp.lanscope.domain.{CompanyId, SystemError}
import jp.lanscope.domain.device.{Device, DeviceRepository, Os}
import jp.lanscope.presentation.core.HttpStatusCode
import org.scalamock.matchers.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.diagrams.Diagrams
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

import scala.jdk.CollectionConverters._

class BaseSpec extends AnyWordSpec with GivenWhenThen with Matchers with MockFactory with Diagrams {
  "Base" when {
    "device_nameありのリクエストが送信された時" should {
      "StatusCode: 200が返されること" in new WithFixture {
        (mockDeviceRepository
          .create(_: Device))
          .expects(where { (device: Device) =>
            device.companyId should be(CompanyId("TEST_COMPANY"))
            device.os should be(Os("iOS"))
            device.deviceName should be(Some("TEST_DEVICE"))
            true
          })
          .returns(Right(()))
          .once()

        When("処理実行")
        private val request = new APIGatewayProxyRequestEvent {
          setPathParameters(Map("group_id" -> "TEST_GROUP_ID").asJava)
          setRequestContext(new ProxyRequestContext {
            setAuthorizer(
              Map(
                "data" ->
                  s"""{
                     |"company_id": "TEST_COMPANY_ID"
                     |}""".stripMargin.asInstanceOf[Object]
              ).asJava
            )
          })
          setBody(s"""{
                     |"company_id": "TEST_COMPANY",
                     |"os": "iOS",
                     |"device_name": "TEST_DEVICE"
                     |}""".stripMargin)
        }

        private val actual = handle(request)
        Then("200 OKが返される")
        println(actual)
        actual.getStatusCode shouldBe HttpStatusCode.OK
      }
    }

    "device_nameなしのリクエストが送信された時" should {
      "StatusCode: 200が返されること" in new WithFixture {
        (mockDeviceRepository
          .create(_: Device))
          .expects(where { (device: Device) =>
            device.companyId should be(CompanyId("TEST_COMPANY"))
            device.os should be(Os("iOS"))
            true
          })
          .returns(Right(()))
          .once()

        When("処理実行")
        private val request = new APIGatewayProxyRequestEvent {
          setPathParameters(Map("group_id" -> "TEST_GROUP_ID").asJava)
          setRequestContext(new ProxyRequestContext {
            setAuthorizer(
              Map(
                "data" ->
                  s"""{
                     |"company_id": "TEST_COMPANY_ID"
                     |}""".stripMargin.asInstanceOf[Object]
              ).asJava
            )
          })
          setBody(s"""{
                     |"company_id": "TEST_COMPANY",
                     |"os": "iOS"
                     |}""".stripMargin)
        }

        private val actual = handle(request)
        Then("200 OKが返される")
        println(actual)
        actual.getStatusCode shouldBe (HttpStatusCode.OK)
      }
    }

    "必須キーなしのリクエストが送信された時" should {
      "StatusCode: 400が返されること" in new WithFixture {
        (mockDeviceRepository
          .create(_: Device))
          .expects(*)
          .never()

        When("処理実行")
        private val request = new APIGatewayProxyRequestEvent {
          setPathParameters(Map("group_id" -> "TEST_GROUP_ID").asJava)
          setRequestContext(new ProxyRequestContext {
            setAuthorizer(
              Map(
                "data" ->
                  s"""{
                     |"company_id": "TEST_COMPANY_ID"
                     |}""".stripMargin.asInstanceOf[Object]
              ).asJava
            )
          })
          setBody(s"""{
                     |"os": "iOS"
                     |}""".stripMargin)
        }

        private val actual = handle(request)
        Then("400 が返される")
        println(actual)
        actual.getStatusCode shouldBe (HttpStatusCode.BadRequest)
      }
    }

    "createに失敗した時" should {
      "StatusCode: 500が返されること" in new WithFixture {
        (mockDeviceRepository
          .create(_: Device))
          .expects(where { (device: Device) =>
            device.companyId should be(CompanyId("TEST_COMPANY"))
            device.os should be(Os("iOS"))
            device.deviceName should be(Some("TEST_DEVICE"))
            true
          })
          .returns(Left(SystemError("", new RuntimeException(""))))
          .once()

        When("処理実行")
        private val request = new APIGatewayProxyRequestEvent {
          setPathParameters(Map("group_id" -> "TEST_GROUP_ID").asJava)
          setRequestContext(new ProxyRequestContext {
            setAuthorizer(
              Map(
                "data" ->
                  s"""{
                     |"company_id": "TEST_COMPANY_ID"
                     |}""".stripMargin.asInstanceOf[Object]
              ).asJava
            )
          })
          setBody(s"""{
                     |"company_id": "TEST_COMPANY",
                     |"os": "iOS",
                     |"device_name": "TEST_DEVICE"
                     |}""".stripMargin)
        }

        private val actual = handle(request)
        Then("500 が返される")
        println(actual)
        actual.getStatusCode shouldBe (HttpStatusCode.InternalServerError)
      }
    }
  }

  trait WithFixture extends Base {
    lazy val mockDeviceRepository: DeviceRepository = mock[DeviceRepository]
    val deviceRepository: DeviceRepository          = mockDeviceRepository
  }
}
