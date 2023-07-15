package jp.lanscope.infrastracture.dynamodb

import cats.effect.IO
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model._

import scala.annotation.tailrec
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

trait DynamoDBWrapper {
  protected val region: Region
  protected val tableName: String
  protected val dynamoDBClient: DynamoDbClient = DynamoDbClient
    .builder()
    .region(region)
    .build()

  protected val RETRY_COUNT                    = 3
  protected val BATCH_WRITE_DYNAMODB_MAX_COUNT = 25

  protected def find[E](primaryKeyName: String, primaryKeyValue: String)(implicit
      itemToEntity: Option[Map[String, AttributeValue]] => E
  ): Try[Option[E]] =
    for {
      request <- Try(
        GetItemRequest
          .builder()
          .tableName(tableName)
          .key(Map(primaryKeyName -> AttributeValue.builder().s(primaryKeyValue).build()).asJava)
          .build()
      )
      response <- Try(dynamoDBClient.getItem(request))
      entity   <- if (response.hasItem) Try(Some(itemToEntity(Option(response.item().asScala.toMap)))) else Try(None)
    } yield entity

  protected def find[E](primaryKeyName: String, primaryKeyValue: String, sortKeyName: String, sortKeyValue: String)(implicit
      itemToEntity: Map[String, AttributeValue] => E
  ): Try[Option[E]] =
    for {
      request <- Try(
        GetItemRequest
          .builder()
          .tableName(tableName)
          .key(
            Map(
              primaryKeyName -> AttributeValue.builder().s(primaryKeyValue).build(),
              sortKeyName    -> AttributeValue.builder().s(sortKeyValue).build()
            ).asJava
          )
          .build()
      )
      response <- Try(dynamoDBClient.getItem(request))
      entity   <- if (response.hasItem) Try(Some(itemToEntity(response.item().asScala.toMap))) else Try(None)
    } yield entity

  protected def put[E](item: Map[String, AttributeValue]): Try[Map[String, AttributeValue]] =
    for {
      _ <- Try(
        dynamoDBClient.putItem(
          PutItemRequest
            .builder()
            .tableName(tableName)
            .item(item.asJava)
            .build()
        )
      )
    } yield item

  protected def put2[E](item: Map[String, AttributeValue]): IO[Map[String, AttributeValue]] =
    for {
      _ <- IO(
        dynamoDBClient.putItem(
          PutItemRequest
            .builder()
            .tableName(tableName)
            .item(item.asJava)
            .build()
        )
      )
    } yield item

  protected def put[E](item: Map[String, AttributeValue], conditionExpression: String): Try[Map[String, AttributeValue]] =
    for {
      request <- Try(
        PutItemRequest
          .builder()
          .tableName(tableName)
          .item(item.asJava)
          .conditionExpression(conditionExpression)
          .build()
      )
      _ <- Try(dynamoDBClient.putItem(request))
    } yield item

  protected def update(
      key: Map[String, AttributeValue],
      updateExpression: String,
      attributeValue: Map[String, AttributeValue],
      attributeName: Map[String, String]
  ): Try[Unit] =
    for {
      request <- Try(
        UpdateItemRequest
          .builder()
          .tableName(tableName)
          .key(key.asJava)
          .updateExpression(updateExpression)
          .expressionAttributeNames(attributeName.asJava)
          .expressionAttributeValues(attributeValue.asJava)
          .build()
      )
      _ <- Try(dynamoDBClient.updateItem(request))
    } yield ()

  protected def update(
      key: Map[String, AttributeValue],
      updateExpression: String,
      conditionExpression: String,
      attributeName: Map[String, String],
      attributeValue: Map[String, AttributeValue]
  ): Try[Unit] =
    for {
      request <- Try(
        UpdateItemRequest
          .builder()
          .tableName(tableName)
          .key(key.asJava)
          .updateExpression(updateExpression)
          .conditionExpression(conditionExpression)
          .expressionAttributeNames(attributeName.asJava)
          .expressionAttributeValues(attributeValue.asJava)
          .build()
      )
      _ <- Try(dynamoDBClient.updateItem(request))
    } yield ()

  protected def delete(item: Map[String, AttributeValue]): Try[Map[String, AttributeValue]] =
    for {
      request <- Try(
        DeleteItemRequest
          .builder()
          .tableName(tableName)
          .key(item.asJava)
          .build()
      )
      _ <- Try(dynamoDBClient.deleteItem(request))
    } yield item

  protected def delete2(item: Map[String, AttributeValue]): IO[Map[String, AttributeValue]] =
    for {
      request <- IO(
        DeleteItemRequest
          .builder()
          .tableName(tableName)
          .key(item.asJava)
          .build()
      )
      _ <- IO(dynamoDBClient.deleteItem(request))
    } yield item

  protected def delete(
      item: Map[String, AttributeValue],
      conditionExpression: String,
      expressionAttributeValues: Map[String, String]
  ): Try[Map[String, AttributeValue]] =
    for {
      request <- Try(
        DeleteItemRequest
          .builder()
          .tableName(tableName)
          .key(item.asJava)
          .conditionExpression(conditionExpression)
          .expressionAttributeNames(expressionAttributeValues.asJava)
          .build()
      )
      _ <- Try(dynamoDBClient.deleteItem(request))
    } yield item

  protected def query[E](primaryKeyName: String, primaryKeyValue: String)(implicit
      itemToEntity: Map[String, AttributeValue] => E
  ): Try[Seq[E]] =
    for {
      request <- Try(
        QueryRequest
          .builder()
          .tableName(tableName)
          .keyConditionExpression(s"$primaryKeyName = :p")
          .expressionAttributeValues(
            Map(
              ":p" -> AttributeValue.builder().s(primaryKeyValue).build()
            ).asJava
          )
          .build()
      )
      responses <- Try(
        dynamoDBClient
          .queryPaginator(request)
          .iterator()
          .asScala
          .flatMap(v => v.items().asScala.map(_.asScala.toMap))
          .toSeq
      )
      entities <- Try(responses.map(itemToEntity))
    } yield entities

  protected def query[E](indexName: String, primaryKeyName: String, primaryKeyValue: String, sortKeyName: String, sortKeyValue: String)(
      itemToEntity: Map[String, AttributeValue] => E
  ): Try[Option[Seq[E]]] =
    (for {
      request <- Try(
        QueryRequest
          .builder()
          .tableName(tableName)
          .indexName(indexName)
          .keyConditionExpression(s"$primaryKeyName = :p and $sortKeyName = :s")
          .expressionAttributeValues(
            Map(
              ":p" -> AttributeValue.builder().s(primaryKeyValue).build(),
              ":s" -> AttributeValue.builder().s(sortKeyValue).build()
            ).asJava
          )
          .build()
      )
      responses <- Try(
        dynamoDBClient
          .queryPaginator(request)
          .iterator()
          .asScala
          .flatMap(v => v.items().asScala.map(_.asScala.toMap))
          .toSeq
      )
      entities <- Try(responses.map(itemToEntity))
    } yield entities).fold(
      e => Failure(e),
      {
        case v if v.nonEmpty => Success(Some(v))
        case _               => Success(None)
      }
    )

  // TODO: nishi retryOverとかのテストを追加する
  @tailrec
  private def internalBatchWriteUnProcessItems(response: BatchWriteItemResponse, retryCount: Int): Try[BatchWriteItemResponse] =
    response.unprocessedItems.asScala match {
      case items if items.nonEmpty && 0 < retryCount =>
        Try(
          dynamoDBClient.batchWriteItem(
            BatchWriteItemRequest
              .builder()
              .requestItems(response.unprocessedItems)
              .build()
          )
        ) match {
          case Success(v) =>
            Thread.sleep((RETRY_COUNT - retryCount) * 1000)
            internalBatchWriteUnProcessItems(v, retryCount - 1)
          case Failure(_) =>
            Thread.sleep((RETRY_COUNT - retryCount) * 1000)
            internalBatchWriteUnProcessItems(response, retryCount - 1)
        }
      case items if items.nonEmpty && 0 == retryCount => Failure(new RuntimeException("internalBatchWriteUnProcessItems. RetryOver"))
      case _                                          => Success(response)
    }

  private def batchWriteUnProcessItems(response: BatchWriteItemResponse, retryCount: Int): Try[BatchWriteItemResponse] =
    internalBatchWriteUnProcessItems(response, retryCount)

  @tailrec
  private def internalBatchWriteItems(items: Seq[WriteRequest]): Try[Unit] =
    (for {
      splitItems <- Try(items.splitAt(BATCH_WRITE_DYNAMODB_MAX_COUNT))
      response <- Try(
        dynamoDBClient.batchWriteItem(
          BatchWriteItemRequest.builder
            .requestItems(Map(tableName -> splitItems._1.asJava).asJava)
            .build()
        )
      )
      _ <- batchWriteUnProcessItems(response, 3)
    } yield splitItems) match {
      case Failure(e) => Failure(e)
      case Success(v) =>
        v._2 match {
          case Nil => Success(())
          case _   => internalBatchWriteItems(v._2)
        }
    }

  def batchWriteItems(items: Seq[Map[String, AttributeValue]]): Try[Unit] =
    Try {
      items.map(i =>
        WriteRequest
          .builder()
          .putRequest(PutRequest.builder().item(i.asJava).build())
          .build()
      ) match {
        case v: Seq[WriteRequest] if v.isEmpty => Success(())
        case v: Seq[WriteRequest]              => internalBatchWriteItems(v)
        case _                                 => Failure(new RuntimeException(s"batchWriteItems Error. $items"))
      }
    }
}
