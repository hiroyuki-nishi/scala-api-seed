package jp.lanscope.infrastructure.s3

import jp.lanscope.domain.DateTimeUtil
import org.scalamock.scalatest.MockFactory
import org.scalatest.diagrams.Diagrams
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model._
import software.amazon.awssdk.services.s3.presigner.S3Presigner

import java.net.URI
import java.time.{ZoneId, ZonedDateTime}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.jdk.CollectionConverters._

class S3WrapperSpec extends AnyWordSpec with Diagrams with BeforeAndAfterAll with BeforeAndAfter with MockFactory {

  private val _s3Client = S3Client
    .builder()
    .region(Region.AP_NORTHEAST_1)
    .endpointOverride(new URI("http://localhost:4566"))
    .build()

  private val _s3Presigner = S3Presigner
    .builder()
    .region(Region.AP_NORTHEAST_1)
    .endpointOverride(new URI("http://localhost:4566"))
    .build()

  val _bucketName: String = "test-bucket-local"

  override def beforeAll(): Unit = {
    println(s"***** beforeAll *****")
    _s3Client.createBucket(
      CreateBucketRequest.builder().bucket(_bucketName).build()
    )
  }

  def clearBucket(bucket: String): Unit = {
    val obj   = _s3Client.listObjects(ListObjectsRequest.builder().bucket(bucket).build())
    val toDel = obj.contents().asScala.map { o => ObjectIdentifier.builder().key(o.key()).build() }
    if (toDel.nonEmpty) {
      _s3Client.deleteObjects(
        DeleteObjectsRequest
          .builder()
          .bucket(bucket)
          .delete(
            Delete.builder().objects(toDel.asJava).build()
          )
          .build()
      )
    }
  }

  override def afterAll(): Unit = {
    println(s"***** afterAll *****")
    clearBucket(_bucketName)
    _s3Client.deleteBucket(
      DeleteBucketRequest.builder().bucket(_bucketName).build()
    )
  }

  "findObject" when {
    "オブジェクトが1件S3にあり、S3から取得した時" should {
      "オブジェクトが取得できる" in new WithFixture {
        putObject("test", "TEST".getBytes)
        private val actual = findObject("test")
        actual.isSuccess should be(true)
        actual.get.nonEmpty should be(true)
        new String(actual.get.get) == "TEST" should be(true)
      }
    }

    "オブジェクトがS3に存在しないS3から取得した時" should {
      "取得に失敗すること" in new WithFixture {
        clearBucket(_bucketName)

        private val actual = findObject("test")
        actual.isFailure should be(true)
        println(actual)
      }
    }

    "deleteObject" when {
      "1件のオブジェクトがある場合" should {
        "1件オブジェクトを削除できる" in new WithFixture {
          putObject("test", "TEST".getBytes)
          private val actual = deleteObject("test")
          actual.isSuccess should be(true)
          println(actual)
        }
      }

      "対象のオブジェクトがない場合に削除した時" should {
        "Success(DeleteObjectResponse())になること" in new WithFixture {
          private val actual = deleteObject("test")
          actual.isSuccess should be(true)
          println(actual)
        }
      }
    }

    "findPreSignedUrl" when {
      "対象のオブジェクトがある場合" should {
        "署名付きURLが返される" in new WithFixture {
          putObject("test", "TEST".getBytes)
          private val actual = createPreSignedUrl("test", "テストファイル", 10 second)
          actual.isSuccess should be(true)
          println(actual)
        }
      }

      // TODO: nishi この動きでいいか疑問
      "対象のオブジェクトがない場合" should {
        "署名付きURLが返される" in new WithFixture {
          clearBucket(_bucketName)
          private val actual = createPreSignedUrl("xxxxx", "テストファイル", 10 second)
          actual.isSuccess should be(true)
          println(actual)
        }
      }
    }

    "createMultiPartUploadId" when {
      "マルチパートアップロード用のuplodaIDを作成した時" should {
        "uplodaIDが返される" in new WithFixture {
          private val actual = createMultiPartUploadId(
            "TEST_MULTI_PART_KEY",
            DateTimeUtil.toInstant(ZonedDateTime.of(2021, 3, 21, 23, 59, 59, 0, ZoneId.of("UTC")))
          )
          println(actual)
          actual.isSuccess should be(true)
        }
      }
    }

    trait WithFixture extends S3Wrapper {
      override protected val bucketName: String       = _bucketName
      override protected val s3Client: S3Client       = _s3Client
      override protected val s3Presigner: S3Presigner = _s3Presigner
    }
  }
}
