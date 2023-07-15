package jp.device.getdevices

import io.circe.Encoder
import io.circe.Json.{arr, fromString, obj}
import io.circe.syntax.EncoderOps
import jp.lanscope.domain.device.{Device, Devices}

//object JsonProtocol {
//  implicit val encoder: Encoder[Device] = (b: Device) => {
//    obj(
//      Seq(
//        Some("title" -> fromString(b.title.value)),
//        Some("os"    -> fromString(b.os.value))
//      ).flatten: _*
//    )
//  }
//
//  implicit val decoder: Decoder[Device] = (c: HCursor) =>
//    for {
//      title <- c.downField("title").as[String]
//      os    <- c.downField("os").as[String]
//    } yield Device(
//      Title(title),
//      Os(os)
//    )
//}
//

object Dto {
//  case class DevicesRequest(companyId: CompanyId)
//  implicit val devicesRequestDecoder: Decoder[DevicesRequest] = deriveDecoder[DevicesRequest]

  implicit val deviceEncoder: Encoder[Device] = { (o: Device) =>
    obj(
      Seq(
        Some("uuid"                    -> fromString(o.uuid.value)),
        Some("os"                      -> fromString(o.os.value)),
        o.deviceName.map("device_name" -> fromString(_))
      ).flatten: _*
    )
  }

  implicit val encoder: Encoder[Devices] = { (response: Devices) =>
    obj(
      Seq(
        Some("data" -> arr(response.items.map(_.asJson): _*))
      ).flatten: _*
    )
  }
}
