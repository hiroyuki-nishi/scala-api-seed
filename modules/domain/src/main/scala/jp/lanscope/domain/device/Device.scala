package jp.lanscope.domain.device

import jp.lanscope.domain.{CompanyId, UuId}

case class Title(value: String) extends AnyVal
case class Os(value: String)    extends AnyVal

case class Device(companyId: CompanyId, uuid: UuId, os: Os, deviceName: Option[String], updatedAt: String)
case class Devices(items: Seq[Device])
