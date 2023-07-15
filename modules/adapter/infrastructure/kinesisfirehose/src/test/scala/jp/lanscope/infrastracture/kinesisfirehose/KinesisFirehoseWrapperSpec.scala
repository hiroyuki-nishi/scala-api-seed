package jp.lanscope.infrastracture.kinesisfirehose

import org.scalamock.scalatest.MockFactory
import org.scalatest.diagrams.Diagrams
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.firehose.FirehoseClient
import software.amazon.awssdk.services.firehose.model.{CreateDeliveryStreamRequest, DeleteDeliveryStreamRequest}

import java.net.URI
import scala.util.Try

class KinesisFirehoseWrapperSpec extends AnyWordSpec with GivenWhenThen with Diagrams with BeforeAndAfterAll with MockFactory {
  private val testKinesisName = "temp-ut"
  private val firehoseLocal = FirehoseClient
    .builder()
    .region(Region.US_EAST_1)
    .endpointOverride(new URI("http://localhost:4566"))
    .build()

  override def beforeAll(): Unit = {
    Try {
      firehoseLocal.createDeliveryStream(
        CreateDeliveryStreamRequest
          .builder()
          .deliveryStreamName(testKinesisName)
          .build()
      )
    }
  }

  override def afterAll(): Unit = {
    Try {
      firehoseLocal.deleteDeliveryStream(
        DeleteDeliveryStreamRequest.builder().deliveryStreamName(testKinesisName).build()
      )
    }
  }

  "putRecords" when {
    "1件データを送信する場合" should {
      "1件データを送信できる" in new WithFixture {
        When("データ送信実行")
        private val actual = putRecords(Seq("TEST".getBytes))

        Then("1件データが取得される")
        println(actual)
        actual.isSuccess should be(true)
      }
    }

    "空データを送信する場合" should {
      "データを送信しない" in new WithFixture {
        When("データ送信実行")
        private val actual = putRecords(Seq.empty[Array[Byte]])

        Then("1件もデータが取得されない")
        println(actual)
        actual.isSuccess should be(true)
      }
    }

    "100件データを送信する場合" should {
      "101件データを送信できる" in new WithFixture {
        When("データ送信実行")
        private val actual = putRecords(Range(1, BATCH_WRITE_FIREHOSE_MAX_COUNT + 1).map(i => s"TEST_$i".getBytes))
        Then("101件データが取得される")
        println(actual)
        actual.isSuccess should be(true)
      }
    }
  }

  trait WithFixture extends KinesisFirehoseWrapper {
    override val firehoseClient: FirehoseClient                = firehoseLocal
    override protected val streamName: String                  = testKinesisName
    override protected val region: Region                      = Region.AP_NORTHEAST_1
    override protected val BATCH_WRITE_FIREHOSE_MAX_COUNT: Int = 100
    override protected val RETRY_COUNT: Int                    = 3
  }
}
