package jp.lanscope.ctxname.presentation.consumer.xxx

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage
import org.scalamock.matchers.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.diagrams.Diagrams
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import scala.jdk.CollectionConverters._

class BaseSpec extends AnyWordSpec with GivenWhenThen with Matchers with MockFactory with Diagrams {

  "handleRequest" when {
    "正常なbodyが送信された時" should {
      "監査用のログが出力されること" in new WithFixture {
        When("処理実行")
        private val sqsEvent = new SQSEvent()
        sqsEvent.setRecords(Seq(createDummySqsMessage(body)).asJava)
        private val actual = handleRequest(sqsEvent, mock[Context])
        println(actual)
        actual.isInstanceOf[Unit]
      }
    }
  }

  "messageReceived" when {
    "正常なbodyが送信された時" should {
      "Right(Unit)が返されること" in new WithFixture {
        When("処理実行")
        private val actual = messageReceived(createDummySqsMessage(body))
        println(actual)
        actual.isRight should be(true)
        actual.getOrElse(Left("")).isInstanceOf[Unit] should be(true)
      }
    }

    // TODO: nishi 実装予定
//    "異常なbodyが送信されパースに失敗した時" should {
//      "Left(???)が返されること" in new WithFixture {
//        When("処理実行")
//        private val actual = messageReceived(createDummySqsMessage(body))
//        println(actual)
//        actual.isLeft should be (true)
//      }
//    }
  }

  trait WithFixture extends Base {
    protected val body: String =
      s"""{
         |"data":
         |  "ewogICJzY2hlbWFfdmVyc2lvbiI6IDIsCiAgImV2ZW50IjogewogICAgImNvbXBhbnlfaWQiOiAiMTc5QUYzQTk3RUIzNEM3Qjg3RkVCODZDN0NDQkUwMUEiLAogICAgImNvcnJlbGF0aW9uX2lkIjogIkNPTlRSQUNUU19QT1NULUZBQzI5RDQ0MDg5MjQ2RkM4QUVBMEUzMEY2QzMzNzE5IiwKICAgICJkYXRldGltZSI6ICIyMDIxLTAxLTA3VDA1OjU2OjUxLjY3NloiLAogICAgImlkIjogIkNPTlRSQUNUX0RFVEVDVF9VUERBVEVELUREMTU2MjZDODcxMTRDNDU5M0NDMEQ3ODBFODA0Q0RDIiwKICAgICJkYXRhX3NjaGVtYV92ZXJzaW9uIjogMSwKICAgICJyZWdpb24iOiAiYXAtbm9ydGhlYXN0LTEiLAogICAgInR5cGUiOiAiQ09OVFJBQ1RfREVURUNUX1VQREFURUQiLAogICAgImVudiI6ICJkZXZvciIKICB9LAogICJkYXRhIjogewogICAgImNvbXBhbnlfaWQiOiAiMTc5QUYzQTk3RUIzNEM3Qjg3RkVCODZDN0NDQkUwMUEiLAogICAgInN0YXJ0X2RhdGUiOiAiMjAyMTAxMDciLAogICAgImxpY2Vuc2UiOiAxMCwKICAgICJpZCI6ICI2OERDNzYyNkRGNjQ0QzFBOUM3MkZCRjBBMTYyRkZCQSIsCiAgICAib3B0aW9uX3BsYW5zIjogWwogICAgICB7CiAgICAgICAgImlkIjogIlRIUkVBVF9QUk9URUNUSU9OIgogICAgICB9CiAgICBdLAogICAgImVuZF9kYXRlIjogIjIwMjIwMTAxIiwKICAgICJiYXNpY19wbGFuX2lkIjogIkJBU0lDIgogIH0KfQ=="
         |}""".stripMargin

    protected def createDummySqsMessage(body: String): SQSMessage =
      new SQSMessage {
        setMessageId("DUMMY_TEST_MESSAGE_ID")
        setReceiptHandle(
          "ewogICJjb21wYW55X2lkIjogIkRVTU1ZQ09NUEFOWUlEIiwKICAiYmFzaWNfcGxhbl9pZCI6ICJMSUdIVF9BX1RSSUFMIgp9"
        )
        setBody(body)
        setMd5OfBody("38e7ce1d7448a003d47b05b2ca82dff1")
        setAwsRegion("ap-northeast-1")
        setEventSource("aws:sqs")
        setEventSourceArn("arn:aws:sqs:ap-northeast-1:358925038325:xxxxxxxx-devwh")
      }
  }
}
