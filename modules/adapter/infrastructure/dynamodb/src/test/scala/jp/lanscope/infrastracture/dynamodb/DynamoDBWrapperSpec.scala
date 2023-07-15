package jp.lanscope.infrastracture.dynamodb

import org.scalamock.scalatest.MockFactory
import org.scalatest.diagrams.Diagrams
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model._

import java.net.URI
import scala.util.{Success, Try}

case class Result(id: String, range: String, version: Option[Int])

class DynamoDBWrapperSpec extends AnyWordSpec with GivenWhenThen with Diagrams with BeforeAndAfterAll with MockFactory {
  private val testTableName = "temp-ut"
  private val dynamoDBLocal = DynamoDbClient
    .builder()
    .region(Region.US_EAST_1)
    .endpointOverride(new URI("http://localhost:8000"))
    .build()
  private val AttrPrimaryKey = "hk"
  private val AttrRangeKey   = "rk"
  private val AttrVersionKey = "version"

  override def beforeAll(): Unit = {
    Try {
      dynamoDBLocal.createTable(
        CreateTableRequest
          .builder()
          .tableName(testTableName)
          .keySchema(
            KeySchemaElement.builder().attributeName(AttrPrimaryKey).keyType(KeyType.HASH).build(),
            KeySchemaElement.builder().attributeName(AttrRangeKey).keyType(KeyType.RANGE).build()
          )
          .attributeDefinitions(
            AttributeDefinition.builder().attributeName(AttrPrimaryKey).attributeType(ScalarAttributeType.S).build(),
            AttributeDefinition.builder().attributeName(AttrRangeKey).attributeType(ScalarAttributeType.S).build()
          )
          .provisionedThroughput(
            ProvisionedThroughput.builder().readCapacityUnits(1L).writeCapacityUnits(1L).build()
          )
          .build()
      )
    }
  }

  override def afterAll(): Unit = {
    Try {
      dynamoDBLocal.deleteTable(
        DeleteTableRequest.builder().tableName(testTableName).build()
      )
    }
  }

  "find" when {
    "1件データがある場合" should {
      "1件データを取得できる" in new WithFixture {
        protected val PrimaryValue = "ID_1"
        protected val RangeValue   = "RANGE_1"
        Given("1件データを追加する")
        put(
          Map(
            AttrPrimaryKey -> AttributeValue.builder().s(PrimaryValue).build(),
            AttrRangeKey   -> AttributeValue.builder().s(RangeValue).build()
          )
        )

        When("データ取得実行")
        private val actual = find[Result](AttrPrimaryKey, PrimaryValue, AttrRangeKey, RangeValue)

        Then("1件データが取得される")
        println(actual)
        actual.isSuccess should be(true)
        actual.get.getOrElse(None).isInstanceOf[Result] should be(true)
      }
    }

    "データがない場合" should {
      "Success(None)が返ってくる" in new WithFixture {
        protected val RangeValue = "RANGE_1"

        When("データ取得実行")
        private val actual = find[Result](AttrPrimaryKey, "2", AttrRangeKey, RangeValue)

        Then("データが取得されない")
        println(actual)
        actual.isSuccess should be(true)
        actual.get.isEmpty should be(true)
      }
    }

    "存在しないキーを指定した場合" should {
      "Failureが返ってくる" in new WithFixture {
        protected val PrimaryValue = "ID_1"
        protected val RangeValue   = "RANGE_1"
        Given("1件データを追加する")
        put(
          Map(
            AttrPrimaryKey -> AttributeValue.builder().s(PrimaryValue).build(),
            AttrRangeKey   -> AttributeValue.builder().s(RangeValue).build()
          )
        )

        When("データ取得実行")
        private val actual =
          find("dummy_primary_key", PrimaryValue, "dummy_range_key", RangeValue)(itemToEntity)

        Then("データが取得されない")
        println(actual)
        actual.isFailure should be(true)
      }
    }
  }

  "put" when {
    "2件データを追加した場合" should {
      "2件データを追加できる" in new WithFixture {
        protected val primaryValue = "ID_1"
        Given("2件データを追加する")
        put(
          Map(
            AttrPrimaryKey -> AttributeValue.builder().s(primaryValue).build(),
            AttrRangeKey   -> AttributeValue.builder().s("RANGE_1").build()
          )
        )
        put(
          Map(
            AttrPrimaryKey -> AttributeValue.builder().s(primaryValue).build(),
            AttrRangeKey   -> AttributeValue.builder().s("RANGE_2").build()
          )
        )

        When("データ取得実行")
        private val actual = query(AttrPrimaryKey, primaryValue)(itemToEntity)

        Then("2件データが取得される")
        println(actual)
        actual.isSuccess should be(true)
        actual.get.length should be(2)
      }
    }
  }

  "update" when {
    "新しいバージョンのデータで更新した場合" should {
      "更新できること" in new WithFixture {
        protected val primaryValue = "ID_CONDITIONAL_1"
        protected val rangeValue   = "RANGE_CONDITIONAL_1"
        protected val oldVersion   = "1"
        protected val newVersion   = "2"
        Given("1件データを追加する")
        put(
          Map(
            AttrPrimaryKey -> AttributeValue.builder().s(primaryValue).build(),
            AttrRangeKey   -> AttributeValue.builder().s(rangeValue).build(),
            AttrVersionKey -> AttributeValue.builder().n(oldVersion).build()
          )
        ).isSuccess should be(true)

        Given("versionが新しいデータで更新する")
        update(
          key = Map(
            AttrPrimaryKey -> AttributeValue.builder().s(primaryValue).build(),
            AttrRangeKey   -> AttributeValue.builder().s(rangeValue).build()
          ),
          updateExpression = s"set #$AttrVersionKey = :$AttrVersionKey",
          /** version(1)　より version(2)の方が大きかったら */
          conditionExpression = s"#$AttrVersionKey < :$AttrVersionKey",
          attributeName = Map(s"#$AttrVersionKey" -> AttrVersionKey),
          attributeValue = Map(s":$AttrVersionKey" -> AttributeValue.builder().n(newVersion).build())
        ).isSuccess should be(true)

        When("データ取得実行")
        private val actual = query(AttrPrimaryKey, primaryValue)(itemToEntity)

        Then("1件データが取得される")
        println(actual)
        actual.isSuccess should be(true)
        actual.get.length should be(1)
        actual.get.head.id should be(primaryValue)
        actual.get.head.range should be(rangeValue)
        actual.get.head.version should be(Some(2))
      }
    }

    "古いバージョンのデータで更新した場合" should {
      "更新に失敗すること" in new WithFixture {
        protected val primaryValue = "ID_CONDITIONAL_1"
        protected val rangeValue   = "RANGE_CONDITIONAL_1"
        protected val oldVersion   = "1"
        protected val newVersion   = "2"
        Given("1件データを追加する")
        put(
          Map(
            AttrPrimaryKey -> AttributeValue.builder().s(primaryValue).build(),
            AttrRangeKey   -> AttributeValue.builder().s(rangeValue).build(),
            AttrVersionKey -> AttributeValue.builder().n(newVersion).build()
          )
        ).isSuccess should be(true)

        Given("versionが新しいデータで更新する")
        update(
          key = Map(
            AttrPrimaryKey -> AttributeValue.builder().s(primaryValue).build(),
            AttrRangeKey   -> AttributeValue.builder().s(rangeValue).build()
          ),
          updateExpression = s"set #$AttrVersionKey = :$AttrVersionKey",
          conditionExpression = s"#$AttrVersionKey < :$AttrVersionKey",
          attributeName = Map(s"#$AttrVersionKey" -> AttrVersionKey),
          attributeValue = Map(s":$AttrVersionKey" -> AttributeValue.builder().n(oldVersion).build())
        ).isFailure should be(true)

        When("データ取得実行")
        private val actual = query(AttrPrimaryKey, primaryValue)(itemToEntity)

        Then("1件データが取得される")
        println(actual)
        actual.isSuccess should be(true)
        actual.get.length should be(1)
        actual.get.head.id should be(primaryValue)
        actual.get.head.range should be(rangeValue)
        actual.get.head.version should be(Some(2))
      }
    }
  }

  "delete" when {
    "1件データがある場合" should {
      "1件データを削除できる" in new WithFixture {
        Given("1件データを追加する")
        val item =
          Map(
            "hk" -> AttributeValue.builder().s("TEST_HASH_KEY").build(),
            "rk" -> AttributeValue.builder().s("TEST_RANGE_KEY").build()
          )
        put(
          item
        )

        When("データ削除実行")
        private val actual = delete(
          item
        )

        Then("1件データが削除される")
        println(actual)
        actual.isSuccess should be(true)
        actual should be(Success(item))
      }
    }

    "該当データがない状態で削除した場合" should {
      "成功する(削除したものとみなされる)" in new WithFixture {
        val item =
          Map(
            "hk" -> AttributeValue.builder().s("TEST_HASH_KEY").build(),
            "rk" -> AttributeValue.builder().s("TEST_RANGE_KEY").build()
          )

        When("データ削除実行")
        private val actual = delete(
          item
        )

        Then("1件データが削除される")
        println(actual)
        actual.isSuccess should be(true)
        actual should be(Success(item))
      }
    }
  }

  "query" when {
    "結果が1MBを超える場合" should {
      "1MBを超える結果も返される" in new WithFixture {
        Given("1MBを超えるデータ(5MB)が存在する")
        private val primaryValue = "DUMMY_HASH_KEY"
        private val items =
          (1 to 1000).map(i =>
            Map(
              "hk" -> AttributeValue.builder().s(primaryValue).build(),
              "rk" -> AttributeValue.builder().s(i.toString).build()
            )
          )

        batchWriteItems(items)

        When("処理実行")
        private val actual = query(AttrPrimaryKey, primaryValue)(itemToEntity)

        Then("全てのデータが取得される")
        println(actual)
        actual.isSuccess should be(true)
        actual.get.length should be(1000)
      }
    }

    // TODO: nishi
    "sortKey指定のユニットテスト追加" should {
      "???" in new WithFixture {}
    }
  }

  "batchWriteItems(requestItems: BatchWriteItemRequest, retryCount: Int)" when {
    "アイテムの数が0の場合" should {
      "正常終了する" in new WithFixture {
        private val actual = batchWriteItems(Seq.empty)
        actual.isSuccess should be(true)
      }
    }

    "アイテムの数が1の場合" should {
      "全てのデータが処理される" in new WithFixture {
        private val items =
          Seq(
            Map(
              "hk" -> AttributeValue.builder().s("1").build(),
              "rk" -> AttributeValue.builder().s("1").build()
            )
          )

        private val actual = batchWriteItems(items)

        actual.isSuccess should be(true)

        private val result = query(AttrPrimaryKey, "1")(itemToEntity)
        result.get.length should be(1)
      }
    }

    "アイテムの数が25の場合" should {
      "全てのデータが処理される" in new WithFixture {
        private val items =
          (1 to 25).map(i =>
            Map(
              "hk" -> AttributeValue.builder().s("25").build(),
              "rk" -> AttributeValue.builder().s(i.toString).build()
            )
          )

        private val actual = batchWriteItems(items)

        actual.isSuccess should be(true)

        private val result = query[Result](AttrPrimaryKey, "25")
        print(result)
        result.get.length should be(25)
        result.get.head.id should be("25")
        result.get.head.range should be("1")
      }
    }

    "アイテムの数が26の場合" should {
      "全てのデータが処理される" in new WithFixture {
        private val items =
          (1 to 26).map(i =>
            Map(
              "hk" -> AttributeValue.builder().s("26").build(),
              "rk" -> AttributeValue.builder().s(i.toString).build()
            )
          )

        private val actual = batchWriteItems(items)

        actual.isSuccess should be(true)

        private val result = query[Result](AttrPrimaryKey, "26")
        print(result)
        result.get.length should be(26)
        result.get.head.id should be("26")
        result.get.head.range should be("1")
      }
    }
  }

  trait WithFixture extends DynamoDBWrapper {
    override val dynamoDBClient: DynamoDbClient   = dynamoDBLocal
    override protected lazy val tableName: String = testTableName
    override protected val region: Region         = Region.AP_NORTHEAST_1

    implicit val itemToEntity: Map[String, AttributeValue] => Result =
      (item: Map[String, AttributeValue]) =>
        Result(
          item(AttrPrimaryKey).s,
          item(AttrRangeKey).s,
          item.get(AttrVersionKey).map(_.n.toInt)
        )
  }
}
