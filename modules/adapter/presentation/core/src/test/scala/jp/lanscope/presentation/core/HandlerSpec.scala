package jp.lanscope.presentation.core

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.ProxyRequestContext
import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent, KinesisEvent, SQSEvent}
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage
import jp.lanscope.domain.AnError
import org.scalamock.matchers.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.diagrams.Diagrams
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

import java.nio.ByteBuffer
import scala.jdk.CollectionConverters._

class HandlerSpec extends AnyWordSpec with GivenWhenThen with Matchers with MockFactory with Diagrams {
  "apiHandleRequest" when {
    "正常なbodyが送信された時" should {
      "監査用のログが出力されること" in new WithApiHandleFixture {
        When("処理実行")
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
      }
    }
  }

  "sqsHandleRequest" when {
    "正常なbodyが送信された時" should {
      "監査用のログが出力されること" in new WithSqsHandleFixture {
        When("処理実行")
        private val sqsEvent = new SQSEvent()
        sqsEvent.setRecords(Seq(createDummySqsMessage(body)).asJava)
        private val actual: Unit = handleRequest(sqsEvent, mock[Context])
        println(actual)
        actual.isInstanceOf[Unit]
      }
    }
  }

  "messageReceived" when {
    "正常なbodyが送信された時" should {
      "Right(Unit)が返されること" in new WithSqsHandleFixture {
        When("処理実行")
        private val actual = messageReceived(createDummySqsMessage(body))
        println(actual)
        actual.isRight should be(true)
        actual.getOrElse(Left("")).isInstanceOf[Unit] should be(true)
      }
    }
  }

  "kinesiHandleRequest" when {
    "正常なrecordが送信された時" should {
      "監査用のログが出力されること" in new WithKinesisHandleFixture {
        When("処理実行")
        private val kinesisEvent: KinesisEvent = new KinesisEvent()
        kinesisEvent.setRecords(Seq(createKinesisEvent).asJava)
        private val actual = handleRequest(kinesisEvent, mock[Context])
        println(actual)
        actual.isInstanceOf[Unit]
      }
    }
  }

  "recordReceived" when {
    "正常なrecordが送信された時" should {
      "Right(Unit)が返されること" in new WithKinesisHandleFixture {
        When("処理実行")
        private val actual = recordReceived(createKinesisEvent)
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

  trait WithApiHandleFixture extends ApiHandler {
    override def handle(input: APIGatewayProxyRequestEvent): APIGatewayProxyResponseEvent = {
      import jp.lanscope.presentation.core.ResponseConverter.ApiResponse
      (for {
        v <- Right("TEST")
      } yield v).toResponse // API側でJSON文字列にするためにimplicitでJsonProtocolを渡す必要がある
    }
  }

  trait WithSqsHandleFixture extends SqsRequestHandler {
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

    override protected def messageReceived(message: SQSMessage): Either[AnError, Unit] = {
      for {
        _ <- Right("Hello World!")
      } yield ()
    }
  }

  trait WithKinesisHandleFixture extends KinesisRequestHandler {
    def createKinesisEvent: KinesisEventRecord = {
      val record = new KinesisEvent.Record()
      record.setPartitionKey("partitionKey-01")
      record.setSequenceNumber("1111111111")
      record.setData(ByteBuffer.wrap(s"""
                                        |{
                                        |  "body": [
                                        |    {
                                        |      "time": "2021-09-10",
                                        |    }
                                        |  ]
                                        |}
        """.stripMargin.getBytes("UTF-8")))

      val kinesisEventRecord = new KinesisEvent.KinesisEventRecord()
      kinesisEventRecord.setKinesis(record)
      kinesisEventRecord.setEventSource("aws:kinesis")
      kinesisEventRecord.setEventID("shardId-000000000000:49545115243490985018280067714973144582180062593244200961")
      kinesisEventRecord.setInvokeIdentityArn("arn:aws:iam::EXAMPLE")
      kinesisEventRecord.setEventVersion("1.0")
      kinesisEventRecord.setEventName("aws:kinesis:record")
      kinesisEventRecord.setEventSourceARN("arn:aws:iam::EXAMPLE")
      kinesisEventRecord.setAwsRegion("ap-northeast-1")
      kinesisEventRecord
    }

    override protected def recordReceived(record: KinesisEventRecord): Either[AnError, Unit] = {
      for {
        _ <- Right("Hello World!")
      } yield ()
    }
  }
}
