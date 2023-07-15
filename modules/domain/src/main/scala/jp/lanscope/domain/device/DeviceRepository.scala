package jp.lanscope.domain.device

import cats.effect.IO
import jp.lanscope.domain.{AnError, CompanyId, UuId}

trait DeviceRepository {
  def findAllBy(companyId: CompanyId): Either[AnError, Seq[Device]]
  def findBy(companyId: CompanyId, uuId: UuId): Either[AnError, Option[Device]]
  def create(device: Device): Either[AnError, Unit]
  def update(device: Device): Either[AnError, Unit]
  def delete(device: Device): Either[AnError, Unit]
  def delete2(device: Device): IO[Device]
}
