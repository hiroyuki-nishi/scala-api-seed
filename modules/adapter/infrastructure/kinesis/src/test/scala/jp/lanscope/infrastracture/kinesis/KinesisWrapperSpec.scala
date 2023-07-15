package jp.lanscope.infrastracture.kinesis

import java.net.URI

import org.scalamock.scalatest.MockFactory
import org.scalatest.diagrams.Diagrams
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.kinesis.KinesisClient
import software.amazon.awssdk.services.kinesis.model.{CreateStreamRequest, DeleteStreamRequest, DescribeStreamRequest}

import scala.util.Try

class KinesisWrapperSpec extends AnyWordSpec with GivenWhenThen with Diagrams with BeforeAndAfterAll with MockFactory {
  private val testKinesisName = "temp-ut"
  private val kinesisLocal = KinesisClient
    .builder()
    .region(Region.US_EAST_1)
    .endpointOverride(new URI("http://localhost:4566"))
    .build()

  override def beforeAll(): Unit = {
    Try {
      kinesisLocal.createStream(
        CreateStreamRequest
          .builder()
          .streamName(testKinesisName)
          .shardCount(1)
          .build()
      )

      while (
        kinesisLocal
          .describeStream(
            DescribeStreamRequest.builder().streamName(testKinesisName).build()
          )
          .streamDescription()
          .shards()
          .isEmpty
      ) {
        print(".")
      }
      println()
    }
  }

  override def afterAll(): Unit = {
    Try {
      kinesisLocal.deleteStream(
        DeleteStreamRequest.builder().streamName(testKinesisName).build()
      )
    }
  }

  "putItem" when {
    "1件データを送信する場合" should {
      "1件データを送信できる" in new WithFixture {
        When("データ送信実行")
        private val actual = putItem("test", "TEST".getBytes)

        Then("1件データが取得される")
        println(actual)
        actual.isSuccess should be(true)
      }
    }
  }

  trait WithFixture extends KinesisWrapper {
    override val kinesisClient: KinesisClient = kinesisLocal
    override protected val streamName: String = testKinesisName
    override protected val region: Region     = Region.AP_NORTHEAST_1
  }
}
