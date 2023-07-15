package jp.device.createdevice

import io.circe.Json.{fromString, obj}
import io.circe.{Decoder, Encoder, HCursor}
import jp.lanscope.domain._
import jp.lanscope.domain.device.{Device, Os}

import scala.util.Try

case class CreateDeviceRequest(companyId: CompanyId, os: Os, deviceName: Option[String]) {
  def create(): Either[AnError, Device] =
    Try(
      Device(companyId, UuId.create, os, deviceName, DateTimeUtil.toISOString(DateTimeUtil.now()))
    ).fold(
      e => Left(SystemError(s"companyID: $companyId, os: $os, deviceName: ${deviceName.getOrElse("")}", e)),
      v => Right(v)
    )
}

object CreateDeviceRequest {
  implicit val encoder: Encoder[Device] = (d: Device) => {
    obj(
      Seq(
        Some("uuid" -> fromString(d.uuid.value))
        //          Some("os"    -> fromString(b.os.value))
      ).flatten: _*
    )
  }

  /**
    * deriveの場合、ネスト内は自分で定義が必要
    */
  implicit val decoder: Decoder[CreateDeviceRequest] = (c: HCursor) =>
    for {
      companyId  <- c.downField("company_id").as[String]
      os         <- c.downField("os").as[String]
      deviceName <- c.downField("device_name").as[Option[String]]
    } yield CreateDeviceRequest(
      CompanyId(companyId),
      Os(os),
      deviceName
    )

  def decoder(body: String): Either[ValidationError, CreateDeviceRequest] = {
    import io.circe.DecodingFailure
    import io.circe.jawn.decode
    import jp.lanscope.domain.{ValidationError, ValidationErrorDetail, ValidationErrorIllegalValue}

    decode[CreateDeviceRequest](body).fold(
      {
        case e: DecodingFailure =>
          Left(ValidationError(body, Seq(ValidationErrorIllegalValue(Some(e.history.toString()), None)), e))
        case e =>
          Left(ValidationError(body, Seq.empty[ValidationErrorDetail], e))
      },
      v => Right(v)
    )
  }
}
