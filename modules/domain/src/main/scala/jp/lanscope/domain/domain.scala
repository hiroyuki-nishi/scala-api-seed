package jp.lanscope

import java.time.{ZonedDateTime}

package object domain {
  trait Enum {
    val value: String
  }

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  trait Enums[E <: Enum] {
    val values: Set[E]
    def valueOf(value: String): E =
      values.find(_.value == value).getOrElse(throw new IllegalArgumentException(s"Undefined value $value."))
  }

  final case class Env(value: String)        extends AnyVal
  final case class AWSRegion(value: String)  extends AnyVal
  final case class MessageId(value: String)  extends AnyVal
  final case class CompanyId(value: String)  extends AnyVal
  final case class ContractId(value: String) extends AnyVal
  final case class AccountId(value: String)  extends AnyVal
  final case class PersonId(value: String)   extends AnyVal
  final case class GroupId(value: String)    extends AnyVal
  final case class ClientId(value: String)   extends AnyVal
  final case class DeviceId(value: String)   extends AnyVal
  final case class UuId(value: String)       extends AnyVal
  object UuId {
    def create: UuId = UuId(java.util.UUID.randomUUID.toString)
  }

  final case class Version(value: Long) extends AnyVal {
    def increment: Version = Version(value + 1)
  }
  final case class CreatedAt(value: ZonedDateTime) extends AnyVal
  final case class CreatedBy(value: String)        extends AnyVal
  final case class UpdatedAt(value: ZonedDateTime) extends AnyVal
  final case class UpdatedBy(value: String)        extends AnyVal
  class TimeStamp(val value: String)
  object TimeStamp {
    def apply(value: ZonedDateTime): TimeStamp = new TimeStamp(DateTimeUtil.toISOString(value))
  }
}
