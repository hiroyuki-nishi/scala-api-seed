package jp.device.getdevices

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.ProxyRequestContext
import jp.device.createdevice.CreateDeviceRequest
import jp.lanscope.domain.device.{Device, DeviceRepository, Os}
import jp.lanscope.domain.{CompanyId, DateTimeUtil, SystemError, UuId}
import org.scalamock.matchers.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.diagrams.Diagrams
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

import scala.jdk.CollectionConverters._

class BaseSpec extends AnyWordSpec with GivenWhenThen with Matchers with MockFactory with Diagrams {
  "handleRequest" when {
    "正常なリクエストが送信された時" should {
      "200が返される" in new WithFixture {
        private val request = new APIGatewayProxyRequestEvent {
          setQueryStringParameters(Map("company_id" -> "DUMMY_COMPANY").asJava)
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
        }

        When("処理実行")
        println(request.getQueryStringParameters.get("company_id"))
        (mockDeviceRepository
          .findAllBy(_: CompanyId))
          .expects(*)
          .returns(
            Right(
              Seq(
                Device(
                  CompanyId("DUMMY_COMPANY"),
                  UuId("DUMMY_UUID"),
                  Os("DUMMY_OS"),
                  Some("斎藤のデバイス"),
                  DateTimeUtil.toISOString(DateTimeUtil.now())
                )
              )
            )
          )
          .once()
        private val actual = handleRequest(request, mock[Context])
        println(actual)

        Then("200 OKが返される")
        actual.getStatusCode should be(200)
      }
    }

    "複数のデバイスが返ってきた時" should {
      "200が返される" in new WithFixture {
        private val request = new APIGatewayProxyRequestEvent {
          setQueryStringParameters(Map("company_id" -> "DUMMY_COMPANY").asJava)
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
        }

        When("処理実行")
        println(request.getQueryStringParameters.get("company_id"))
        (mockDeviceRepository
          .findAllBy(_: CompanyId))
          .expects(where { (companyId: CompanyId) =>
            companyId should be(CompanyId("DUMMY_COMPANY"))
            true
          })
          .returns(
            Right(
              Seq(
                Device(
                  CompanyId("DUMMY_COMPANY"),
                  UuId("DUMMY_UUID"),
                  Os("DUMMY_OS"),
                  Some("斎藤のデバイス"),
                  DateTimeUtil.toISOString(DateTimeUtil.now())
                ),
                Device(
                  CompanyId("DUMMY_COMPANY"),
                  UuId("DUMMY_UUID2"),
                  Os("DUMMY_OS"),
                  Some("斎藤のデバイス2"),
                  DateTimeUtil.toISOString(DateTimeUtil.now())
                )
              )
            )
          )
          .once()
        private val actual = handleRequest(request, mock[Context])
        println(actual)

        Then("200 OKが返される")
        actual.getStatusCode should be(200)
        private val a = CreateDeviceRequest.decoder(actual.getBody)
        println(a)
      }
    }

    "不正なリクエストが送信された時" should {
      "400が返される" in new WithFixture {
        private val request = new APIGatewayProxyRequestEvent {
          setPathParameters(Map("company_id" -> "TEST_GROUP_ID").asJava)
          setRequestContext(new ProxyRequestContext {
            setAuthorizer(
              Map(
                "data" ->
                  s"""{
                     |"company_id": "TEST_COMPANY_ID",
                     |"account_id": "TEST_ACCOUNT_ID",
                     |"person_id": "TEST_PERSON_ID"
                     |}""".stripMargin.asInstanceOf[Object]
              ).asJava
            )
          })
        }

        When("処理実行")
        private val actual = handleRequest(request, mock[Context])
        println(actual)

        Then("400 BadRequestが返される")
        actual.getStatusCode should be(400)
      }
    }

    "nullのリクエストが送信された時" should {
      "ステータスコード：500が返されること" in new WithFixture {
        private val request = new APIGatewayProxyRequestEvent {
          setQueryStringParameters(Map("company_id" -> "TEST_GROUP_ID").asJava)
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
        }

        (mockDeviceRepository
          .findAllBy(_: CompanyId))
          .expects(*)
          .returns(Left(SystemError("", new RuntimeException(""))))
          .once()

        When("処理実行")
        private val actual = handleRequest(request, mock[Context])
        println(actual)

        Then("500 SystemErrorが返される")
        actual.getStatusCode should be(500)
      }
    }
  }

  trait WithFixture extends Base {
    protected val mockDeviceRepository     = mock[DeviceRepository]
    val deviceRepository: DeviceRepository = mockDeviceRepository
  }
}
