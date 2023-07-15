package jp.lanscope.inrastructure.sqs

import org.scalamock.scalatest.MockFactory
import org.scalatest.diagrams.Diagrams
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.{CreateQueueRequest, DeleteQueueRequest, PurgeQueueRequest}

import java.net.URI

class SqsWrapperSpec extends AnyWordSpec with Diagrams with BeforeAndAfterAll with BeforeAndAfter with MockFactory {

  private val _queueName: String = "test-sign-local"
  private val _queueUrl          = s"http://localhost:4566/000000000000/${_queueName}"

  private val _sqsClient = SqsClient
    .builder()
    .region(Region.US_EAST_1)
    .endpointOverride(new URI("http://localhost:4566"))
    .build()

  override def beforeAll(): Unit = {
    println(s"***** beforeAll *****")
    _sqsClient.createQueue(
      CreateQueueRequest.builder().queueName(_queueName).build()
    )
  }

  override def afterAll(): Unit = {
    println(s"***** afterAll *****")
    _sqsClient.deleteQueue(
      DeleteQueueRequest.builder().queueUrl("http://localhost:4576/queue/test-sign-local").build()
    )
  }

  "SqsWrapper" when {
    "メッセージを1件作成してSQSにメッセージを送信した場合" should {
      "送信出来る" in new WithFixture {
        private val actual = sendMessage("test body")
        assert(actual.isSuccess)
        private val receive = receiveMessage(10)
        println(receive)
      }
    }

    "メッセージが1件もない時にSQSからメッセージを取得した場合" should {
      "エラーにならない" in new WithFixture {
        _sqsClient.purgeQueue(
          PurgeQueueRequest
            .builder()
            .queueUrl("http://localhost:4576/queue/test-sign-local")
            .build()
        )

        private val actual = receiveMessage(10)
        assert(actual.isSuccess)
        println(actual)
      }
    }

    "1件のメッセージがある時にSQSからメッセージを取得した場合" should {
      "メッセージを受信出来る" in new WithFixture {
        sendMessage("test body")

        private val actual = receiveMessage(10)
        assert(actual.isSuccess)
        println(actual)
      }
    }

    "2件のメッセージがある時にSQSからメッセージを取得した場合" should {
      "2件メッセージを削除出来る" in new WithFixture {
        sendMessage("message1")
        sendMessage("message2")

        private val response = receiveMessage(1)
        private val actual   = deleteMessage(response.get.receiptHandle.get)
        assert(actual.isSuccess)

        private val result = receiveMessage(10)
        assert(result.get.messages.get.length == 1)
      }
    }

    "削除するメッセージが何らかの理由でSQSからすでに削除されている場合" should {
      "削除に失敗(Failure)すること" in new WithFixture {
        sendMessage("message1")
        private val response      = receiveMessage(1)
        val receiptHandle: String = response.get.receiptHandle.get
        println(receiptHandle)

        _sqsClient.purgeQueue(
          PurgeQueueRequest
            .builder()
            .queueUrl("http://localhost:4576/queue/test-sign-local")
            .build()
        )

        private val actual = deleteMessage(receiptHandle)
        assert(actual.isFailure)
      }
    }
  }

  trait WithFixture extends SqsWrapper {
    override protected val sqsClient: SqsClient = _sqsClient
    override protected val queueUrl: String     = _queueUrl
  }
}
