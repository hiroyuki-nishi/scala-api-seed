package jp.lanscope.infrastracture.dynamodb.device

import cats.effect.IO
import jp.lanscope.domain.device.{Device, Os}
import jp.lanscope.domain._
import org.scalamock.scalatest.MockFactory
import org.scalatest.diagrams.Diagrams
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfter, GivenWhenThen}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model._

import java.net.URI
import java.time.{ZoneId, ZonedDateTime}
import cats.effect.unsafe.implicits.global

class DeviceRepositoryOnDynamoDBSpec extends AnyWordSpec with GivenWhenThen with Diagrams with BeforeAndAfter with MockFactory {
  private val _dynamoDBClient = DynamoDbClient
    .builder()
    .region(Region.AP_NORTHEAST_1)
    .endpointOverride(URI.create("http://localhost:8000"))
    .build()
  private val _tableName = "sample-devices-test"

  private def createTable(): CreateTableResponse = {
    val AttrPrimaryKey = "company_id"
    val AttrSortKey    = "uuid"
    _dynamoDBClient.createTable(
      CreateTableRequest
        .builder()
        .tableName(_tableName)
        .keySchema(
          KeySchemaElement.builder().attributeName(AttrPrimaryKey).keyType(KeyType.HASH).build(),
          KeySchemaElement.builder().attributeName(AttrSortKey).keyType(KeyType.RANGE).build()
        )
        .attributeDefinitions(
          AttributeDefinition.builder().attributeName(AttrPrimaryKey).attributeType(ScalarAttributeType.S).build(),
          AttributeDefinition.builder().attributeName(AttrSortKey).attributeType(ScalarAttributeType.S).build()
        )
        .provisionedThroughput(
          ProvisionedThroughput.builder().readCapacityUnits(1L).writeCapacityUnits(1L).build()
        )
        .build()
    )
  }
  private def deleteTable(): DeleteTableResponse =
    _dynamoDBClient.deleteTable(DeleteTableRequest.builder().tableName(_tableName).build())

  before {
    createTable()
  }

  after {
    deleteTable()
  }

  "find" when {
    "1件データがある場合" should {
      "1件データを取得できる" in new WithFixture {
        Given("1件データを追加する")
        create(
          Device(
            CompanyId("DUMMY_COMPANY"),
            UuId("DUMMY_UUID"),
            Os("DUMMY_OS"),
            Some("DUMMY_DEVICE"),
            DateTimeUtil.toISOString(DateTimeUtil.now())
          )
        )

        When("データ取得実行")
        private val actual = findBy(CompanyId("DUMMY_COMPANY"), UuId("DUMMY_UUID"))

        println(actual)
        actual.isRight should be(true)
        private val actualDevice = actual.getOrElse(None).get
        actualDevice.deviceName should be(Some("DUMMY_DEVICE"))
      }
    }

    "該当データがない場合" should {
      "Right(None)が取得できる" in new WithFixture {
        When("データ取得実行")
        private val actual = findBy(CompanyId("DUMMY_COMPANY"), UuId("DUMMY_UUID"))

        println(actual)
        actual.isRight should be(true)
        private val actualDevice = actual.getOrElse(None)
        actualDevice should be(None)
      }
    }

    "該当テーブルに接続できない場合" should {
      "SystemErrorが取得できる" in new WithFixture {
        Given("テーブル削除")
        deleteTable()

        When("データ取得実行")
        private val actual = findBy(CompanyId("DUMMY_COMPANY"), UuId("DUMMY_UUID"))

        println(actual)
        actual.left.getOrElse(SystemError("", new RuntimeException(""))).getMessage should be("SystemError: item: DUMMY_COMPANY")
        createTable()
      }
    }
  }

  "findAllBy" when {
    "100件データがある場合" should {
      "100件データを取得できる" in new WithFixture {
        Given("100件データを追加する")
        private val MAX = 100
        private val dummyDevices = Range(0, MAX).map(x =>
          Device(
            CompanyId("DUMMY_COMPANY"),
            UuId(x.toString),
            Os("iOS"),
            Some("DUMMY_DEVICE"),
            DateTimeUtil.toISOString(DateTimeUtil.now())
          )
        )
        batchWriteItems(dummyDevices.map(toItem))

        When("データ取得実行")
        private val actual = findAllBy(CompanyId("DUMMY_COMPANY"))

        println(actual)
        actual.isRight should be(true)
        private val actualDevices = actual.getOrElse(Seq.empty[Device])
        actualDevices.length should be(MAX)
        actualDevices.foreach(d => dummyDevices.contains(d) should be(true))
      }
    }

    "データがない場合" should {
      "空配列が返ってくること" in new WithFixture {
        When("データ取得実行")
        private val actual = findAllBy(CompanyId("DUMMY_COMPANY"))

        println(actual)
        actual.isRight should be(true)
        private val actualDevices = actual.getOrElse(Seq.empty[Device])
        actualDevices.length should be(0)
      }
    }

    "該当テーブルに接続できない場合" should {
      "SystemErrorが取得できる" in new WithFixture {
        Given("テーブル削除")
        deleteTable()

        When("データ取得実行")
        private val actual = findAllBy(CompanyId("DUMMY_COMPANY"))

        println(actual)
        actual.left.getOrElse(SystemError("", new RuntimeException(""))).getMessage should be("SystemError: item: DUMMY_COMPANY")
        createTable()
      }
    }
  }

  "create" when {
    "1件データ作成した場合" should {
      "1件データを取得できる" in new WithFixture {
        Given("1件データを追加する")
        create(
          Device(
            CompanyId("DUMMY_COMPANY"),
            UuId("DUMMY_UUID"),
            Os("DUMMY_OS"),
            Some("DUMMY_DEVICE"),
            DateTimeUtil.toISOString(ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")))
          )
        )

        When("データ取得実行")
        private val actual = findBy(CompanyId("DUMMY_COMPANY"), UuId("DUMMY_UUID"))

        println(actual)
        actual.isRight should be(true)
        private val actualDevice = actual.getOrElse(None).get
        actualDevice.deviceName should be(Some("DUMMY_DEVICE"))
      }
    }

    "すでに存在するデータを作成した場合" should {
      "Right(())が取得できる" in new WithFixture {
        private val dummyData = Device(
          CompanyId("DUMMY_COMPANY"),
          UuId("DUMMY_UUID"),
          Os("DUMMY_OS"),
          Some("DUMMY_DEVICE"),
          DateTimeUtil.toISOString(DateTimeUtil.now())
        )
        Given("1件データを追加する")
        create(dummyData)

        When("同じデータを作成")
        create(dummyData).isRight should be(true)

        private val actualDevice = findBy(CompanyId("DUMMY_COMPANY"), UuId("DUMMY_UUID")).getOrElse(None).get
        actualDevice.companyId should be(CompanyId("DUMMY_COMPANY"))
      }
    }

    "該当テーブルに接続できない場合" should {
      "SystemErrorが取得できる" in new WithFixture {
        Given("テーブル削除")
        deleteTable()

        When("データ取得実行")
        private val actual = create(
          Device(
            CompanyId("DUMMY_COMPANY"),
            UuId("DUMMY_UUID"),
            Os("DUMMY_OS"),
            Some("DUMMY_DEVICE"),
            DateTimeUtil.toISOString(ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")))
          )
        )

        println(actual)
        actual.left
          .getOrElse(SystemError("", new RuntimeException("")))
          .isInstanceOf[SystemError] should be(true)
//          .getMessage should be ("SystemError: item: Device(CompanyId(DUMMY_COMPANY),UuId(DUMMY_UUID),Os(DUMMY_OS),Some(DUMMY_DEVICE),Some(???))")
        createTable()
      }
    }
  }

  "update" when {
    "1件データ更新した場合" should {
      "1件データを更新できる" in new WithFixture {
        Given("1件データを追加する")
        create(
          Device(
            CompanyId("DUMMY_COMPANY"),
            UuId("DUMMY_UUID"),
            Os("DUMMY_OS"),
            Some("西のデバイス"),
            DateTimeUtil.toISOString(ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")))
          )
        )

        When("データ更新実行")
        update(
          Device(
            CompanyId("DUMMY_COMPANY"),
            UuId("DUMMY_UUID"),
            Os("DUMMY_OS"),
            Some("斎藤のデバイス"),
            DateTimeUtil.toISOString(ZonedDateTime.of(2021, 1, 1, 1, 0, 0, 0, ZoneId.of("UTC")))
          )
        ).isRight should be(true)

        private val actualDevice = findBy(CompanyId("DUMMY_COMPANY"), UuId("DUMMY_UUID"))
        println(actualDevice)
        actualDevice.isRight should be(true)
        private val device = actualDevice.getOrElse(None).get
        device.deviceName should be(Some("斎藤のデバイス"))
      }
    }

    "古い日時でデータ更新した場合" should {
      "データ更新されない" in new WithFixture {
        Given("1件データを追加する")
        create(
          Device(
            CompanyId("DUMMY_COMPANY"),
            UuId("DUMMY_UUID"),
            Os("DUMMY_OS"),
            Some("西のデバイス"),
            DateTimeUtil.toISOString(ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")))
          )
        )

        When("データ更新実行")
        private val actual = update(
          Device(
            CompanyId("DUMMY_COMPANY"),
            UuId("DUMMY_UUID"),
            Os("DUMMY_OS"),
            Some("斎藤のデバイス"),
            DateTimeUtil.toISOString(ZonedDateTime.of(2020, 1, 1, 1, 0, 0, 0, ZoneId.of("UTC")))
          )
        )
        actual.isLeft should be(true)
        actual.left
          .getOrElse(SystemError("", new RuntimeException("")))
          .isInstanceOf[ExclusiveError] should be(true)
//          .getMessage should be ("ExclusiveError: ExclusiveError: item:Device(CompanyId(DUMMY_COMPANY),UuId(DUMMY_UUID),Os(DUMMY_OS),Some(斎藤のデバイス),2020-01-01T01:00:00.000Z)")

        private val actualDevice = findBy(CompanyId("DUMMY_COMPANY"), UuId("DUMMY_UUID"))
        println(actualDevice)
        actualDevice.isRight should be(true)
        private val device = actualDevice.getOrElse(None).get
        device.deviceName should be(Some("西のデバイス"))
      }
    }

    "存在しないデータを更新した場合" should {
      "SystemErrorになること" in new WithFixture {
        When("データ更新実行")
        update(
          Device(
            CompanyId("DUMMY_COMPANY"),
            UuId("DUMMY_UUID"),
            Os("DUMMY_OS"),
            Some("斎藤のデバイス"),
            DateTimeUtil.toISOString(DateTimeUtil.now())
          )
        ).isLeft should be(true)
      }
    }

    "該当テーブルに接続できない場合" should {
      "SystemErrorが取得できる" in new WithFixture {
        Given("テーブル削除")
        deleteTable()

        When("データ取得実行")
        private val actual = update(
          Device(
            CompanyId("DUMMY_COMPANY"),
            UuId("DUMMY_UUID"),
            Os("DUMMY_OS"),
            Some("斎藤のデバイス"),
            DateTimeUtil.toISOString(DateTimeUtil.now())
          )
        )

        println(actual)
        actual.left
          .getOrElse(SystemError("", new RuntimeException("")))
          .isInstanceOf[SystemError] should be(true)
//          .getMessage should be ("SystemError: item: Device(CompanyId(DUMMY_COMPANY),UuId(DUMMY_UUID),Os(DUMMY_OS),Some(斎藤のデバイス),Some(???))")
        createTable()
      }
    }
  }

  // TODO
  "delete" when {
    "1件データ削除した場合" should {
      "1件データを削除できる" in new WithFixture {
        Given("1件データを追加する")
        create(
          Device(
            CompanyId("DUMMY_COMPANY"),
            UuId("DUMMY_UUID"),
            Os("DUMMY_OS"),
            Some("西のデバイス"),
            DateTimeUtil.toISOString(DateTimeUtil.now())
          )
        )

        When("データ削除実行")
        delete(
          Device(
            CompanyId("DUMMY_COMPANY"),
            UuId("DUMMY_UUID"),
            Os("DUMMY_OS"),
            Some("斎藤のデバイス"),
            DateTimeUtil.toISOString(DateTimeUtil.now())
          )
        )

        private val actual = findBy(CompanyId("DUMMY_COMPANY"), UuId("DUMMY_UUID"))
        println(actual)
        actual.isRight should be(true)
      }
    }

    "存在しないデータを1件削除した場合" should {
      "Right()が返ってくること" in new WithFixture {
        delete(
          Device(
            CompanyId("DUMMY_COMPANY"),
            UuId("DUMMY_UUID"),
            Os("DUMMY_OS"),
            Some("DUMMY_DEVICE"),
            DateTimeUtil.toISOString(DateTimeUtil.now())
          )
        ).isRight shouldBe (true)
      }
    }

    "該当テーブルに接続できない場合" should {
      "SystemErrorが取得できる" in new WithFixture {
        Given("テーブル削除")
        deleteTable()

        When("データ取得実行")
        delete(
          Device(
            CompanyId("DUMMY_COMPANY"),
            UuId("DUMMY_UUID"),
            Os("DUMMY_OS"),
            Some("斎藤のデバイス"),
            DateTimeUtil.toISOString(DateTimeUtil.now())
          )
        ).left
          .getOrElse(SystemError("", new RuntimeException("")))
          .isInstanceOf[SystemError] shouldBe (true)
        createTable()
      }
    }
  }

  "delete2" when {
    "1件データ削除した場合" should {
      "1件データを削除できる" in new WithFixture {
        Given("1件データを追加する")
        create2(
          Device(
            CompanyId("DUMMY_COMPANY"),
            UuId("DUMMY_UUID"),
            Os("DUMMY_OS"),
            Some("西のデバイス"),
            DateTimeUtil.toISOString(DateTimeUtil.now())
          )
        ).handleErrorWith(error => {
          IO.pure(SystemError("", error))
        }).unsafeRunSync()

        When("データ削除実行")
        delete2(
          Device(
            CompanyId("DUMMY_COMPANY"),
            UuId("DUMMY_UUID"),
            Os("DUMMY_OS"),
            Some("斎藤のデバイス"),
            DateTimeUtil.toISOString(DateTimeUtil.now())
          )
        ).unsafeRunSync()

        private val actual = findBy(CompanyId("DUMMY_COMPANY"), UuId("DUMMY_UUID"))
        println(actual)
        actual.isRight should be(true)
      }
    }
  }

  class WithFixture(env: Env = Env("test"), r: Region = Region.AP_NORTHEAST_1) extends DeviceRepositoryOnDynamoDB(env, r) {
    override val dynamoDBClient: DynamoDbClient = _dynamoDBClient
  }
}
