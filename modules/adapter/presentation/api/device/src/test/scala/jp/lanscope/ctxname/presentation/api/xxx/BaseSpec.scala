package jp.lanscope.ctxname.presentation.api.xxx

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.ProxyRequestContext
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.diagrams.Diagrams
import org.scalamock.matchers.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

import scala.jdk.CollectionConverters._

class BaseSpec extends AnyWordSpec with GivenWhenThen with Matchers with MockFactory with Diagrams {
  "handleRequest" when {
    "正常なリクエストが送信された時" should {
      "200が返される" in new WithFixture {
        private val request = new APIGatewayProxyRequestEvent {
          setPathParameters(Map("group_id" -> "TEST_GROUP_ID").asJava)
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
          setBody(s"""{
               |"company_id": "TEST_COMPANY_ID",
               |"account_id": "TEST_ACCOUNT_ID",
               |"person_id": "TEST_PERSON_ID"
               |}""".stripMargin)
        }

        When("処理実行")
        private val actual = handleRequest(request, mock[Context])
        println(actual)

        Then("200 OKが返される")
        actual.getStatusCode should be(200)
      }
    }

    "不正なリクエストが送信された時" should {
      "400が返される" in new WithFixture {
        private val request = new APIGatewayProxyRequestEvent {
          setPathParameters(Map("group_id" -> "TEST_GROUP_ID").asJava)
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
            setBody(s"""{
                 |"company_id": "TEST_COMPANY_ID"
                 |"account_id": 1,
                 |"person_id": "TEST_PERSON_ID"
                 |}""".stripMargin)
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
        private val request = null

        When("処理実行")
        private val actual = handleRequest(request, mock[Context])
        println(actual)

        Then("500 SystemErrorが返される")
        actual.getStatusCode should be(500)
      }
    }
  }

  trait WithFixture extends Base {}
}
