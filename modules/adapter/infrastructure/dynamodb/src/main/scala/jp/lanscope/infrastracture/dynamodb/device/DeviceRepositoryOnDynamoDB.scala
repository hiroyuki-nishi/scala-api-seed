package jp.lanscope.infrastracture.dynamodb.device

import cats.effect.IO

import jp.lanscope.domain._
import jp.lanscope.domain.device.{Device, DeviceRepository, Os}
import jp.lanscope.infrastracture.dynamodb.DynamoDBWrapper
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.model.{AttributeValue, ConditionalCheckFailedException}

import scala.util.Try

class DeviceRepositoryOnDynamoDB(env: Env, r: Region) extends DeviceRepository with DynamoDBWrapper {
  protected val region    = r
  protected val tableName = s"sample-devices-${env.value}"

  private val PartitionKeyCompanyId = "company_id"
  private val SortKeyUuId           = "uuid"
  private val AttrKeyOs             = "os"
  private val AttrKeyDeviceName     = "device_name"
  private val AttrKeyUpdatedAt      = "update_at"

  private val toDevice = (item: Map[String, AttributeValue]) =>
    Device(
      companyId = CompanyId(item(PartitionKeyCompanyId).s),
      uuid = UuId(item(SortKeyUuId).s),
      os = Os(item(AttrKeyOs).s),
      deviceName = item.get(AttrKeyDeviceName).map(_.s),
      updatedAt = item(AttrKeyUpdatedAt).s
    )

  def toItem(device: Device): Map[String, AttributeValue] =
    Map(
      Seq(
        Some(PartitionKeyCompanyId -> AttributeValue.builder().s(device.companyId.value).build()),
        Some(SortKeyUuId           -> AttributeValue.builder().s(device.uuid.value).build()),
        Some(AttrKeyOs             -> AttributeValue.builder().s(device.os.value).build()),
        // 必須キーはNoneになる可能性もある
        device.deviceName.map(v => AttrKeyDeviceName -> AttributeValue.builder().s(v).build()),
        Some(AttrKeyUpdatedAt -> AttributeValue.builder().s(device.updatedAt).build())
      ).flatten: _*
    )

  def findAllBy(companyId: CompanyId): Either[AnError, Seq[Device]] = {
    implicit val itemToEntity: Map[String, AttributeValue] => Device =
      (item: Map[String, AttributeValue]) => toDevice(item)
    (for {
      response <- query[Device](PartitionKeyCompanyId, companyId.value)
    } yield response).fold(
      e => Left(SystemError(s"item: ${companyId.value}", e)),
      s => Right(s)
    )
  }

  def findBy(companyId: CompanyId, uuId: UuId): Either[AnError, Option[Device]] = {
    implicit val itemToEntity: Map[String, AttributeValue] => Device = (item: Map[String, AttributeValue]) => toDevice(item)

    (for {
      response <- find[Device](PartitionKeyCompanyId, companyId.value, SortKeyUuId, uuId.value)
    } yield response).fold(
      e => Left(SystemError(s"item: ${companyId.value}", e)),
      s => Right(s)
    )
  }

  def create(device: Device): Either[AnError, Unit] = {
    (for {
      item     <- Try(toItem(device))
      response <- put(item)
    } yield response).fold(
      e => Left(SystemError(s"item: $device", e)),
      _ => Right(())
    )
  }

  def create2(device: Device): IO[Device] =
    for {
      item <- IO(toItem(device))
      _    <- put2(item)
    } yield device

  def update(device: Device): Either[AnError, Unit] = {
    (for {
      response <- update(
        key = Map(
          PartitionKeyCompanyId -> AttributeValue.builder().s(device.companyId.value).build(),
          SortKeyUuId           -> AttributeValue.builder().s(device.uuid.value).build()
        ),
        /**
          * 更新アクション
          * UpdateExpressionにはできることが4つあります。
          *          SETアクション: 値を上書きで保存する
          *          REMOVEアクション: Attributeそのものを消す
          *          ADDアクション: 数値の加減算したり、後述するセット型にデータを追加したりする
          *          DELETEアクション: セット型からデータを削除する
          */
        updateExpression = s"set #$AttrKeyDeviceName = :$AttrKeyDeviceName, #$AttrKeyUpdatedAt= :$AttrKeyUpdatedAt",
        /**
          * 更新条件
          * PrimaryKey(PartitionKey + SortKey)があって、更新日時がない or 古い場合なら更新OK
          */
//        conditionExpression =
        s"#$PartitionKeyCompanyId = :$PartitionKeyCompanyId AND #$SortKeyUuId = :$SortKeyUuId AND (attribute_not_exists(#$AttrKeyUpdatedAt) OR #$AttrKeyUpdatedAt < :$AttrKeyUpdatedAt)",
        //        conditionExpression = s"#$AttrKeyUpdatedAt < :$AttrKeyUpdatedAt",
        attributeName = Map(
          s"#$PartitionKeyCompanyId" -> PartitionKeyCompanyId,
          s"#$SortKeyUuId"           -> SortKeyUuId,
          s"#$AttrKeyDeviceName"     -> AttrKeyDeviceName,
          s"#$AttrKeyUpdatedAt"      -> AttrKeyUpdatedAt
        ),
        /**
          * 更新の値
          */
        attributeValue = Map(
          Seq(
            Some(s":$PartitionKeyCompanyId" -> AttributeValue.builder().s(device.companyId.value).build()),
            Some(s":$SortKeyUuId"           -> AttributeValue.builder().s(device.uuid.value).build()),
            device.deviceName.map(x => s":$AttrKeyDeviceName" -> AttributeValue.builder().s(x).build()),
            Some(s":$AttrKeyUpdatedAt" -> AttributeValue.builder().s(device.updatedAt).build())
          ).flatten: _*
        )
      )
    } yield response).fold(
      {
        case e: ConditionalCheckFailedException =>
          Left(ExclusiveError(s"ExclusiveError: item:$device", e))
        case e =>
          Left(SystemError(s"SystemError: item:$device", e))
      },
      _ => Right(())
    )
  }

  def delete(device: Device): Either[AnError, Unit] = {
    def toItem(device: Device): Try[Map[String, AttributeValue]] =
      Try(
        Map(
          PartitionKeyCompanyId -> AttributeValue.builder().s(device.companyId.value).build(),
          SortKeyUuId           -> AttributeValue.builder().s(device.uuid.value).build()
        )
      )

    (for {
      item     <- toItem(device)
      response <- delete(item)
    } yield response).fold(
      e => Left(SystemError(s"item: $device", e)),
      _ => Right(())
    )
  }

  def delete2(device: Device): IO[Device] = {
    def toItem(device: Device): IO[Map[String, AttributeValue]] =
      IO(
        Map(
          PartitionKeyCompanyId -> AttributeValue.builder().s(device.companyId.value).build(),
          SortKeyUuId           -> AttributeValue.builder().s(device.uuid.value).build()
        )
      )

    for {
      item <- toItem(device)
      _    <- delete2(item)
    } yield device
  }
}
