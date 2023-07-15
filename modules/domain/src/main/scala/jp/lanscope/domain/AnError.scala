package jp.lanscope.domain

import scala.language.implicitConversions
import scala.util.Try

trait ValidationErrorDetail {
  val code: String = "validation_error"
  val reason: String
  val item: Option[String]
  val args: Option[Seq[String]]
}

final case class ValidationErrorRequired(item: Option[String], args: Option[Seq[String]]) extends ValidationErrorDetail {
  override val reason = "required"
}

final case class ValidationErrorOverMaxStrict(item: Option[String], args: Option[Seq[String]]) extends ValidationErrorDetail {
  override val reason = "over_max_strict"
}

final case class ValidationErrorIllegalValue(item: Option[String], args: Option[Seq[String]]) extends ValidationErrorDetail {
  override val reason = "illegal_value"
}

class AnError(message: String, cause: Throwable) extends RuntimeException(message, cause)
final case class ValidationError(item: String, errors: Seq[ValidationErrorDetail], cause: Throwable)
    extends AnError(s"ValidationError: ${errors.toString}", cause)
final case class NotFoundError(item: String, cause: Throwable)                extends AnError(s"NotFoundError: $item", cause)
final case class ExclusiveError(item: String, cause: Throwable)               extends AnError(s"ExclusiveError: $item", cause)
final case class SkipError[T](message: String, cause: Throwable, response: T) extends AnError(s"SkipError: $message", cause)
final case class SystemError(item: String, cause: Throwable)                  extends AnError(s"SystemError: $item", cause)

object Try2EitherSystemErrorImplicit {
  class Try2EitherSystemErrorImplicit[A](a: Try[A]) {
    def toEitherSystemError(item: String): Either[SystemError, A] =
      a.fold(
        e => {
          println(e)
          e.printStackTrace()
          Left[SystemError, A](SystemError(item, e))
        },
        Right[SystemError, A]
      )
  }

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
  implicit def try2EitherSystemErrorImplicit[A](t: Try[A]): Try2EitherSystemErrorImplicit[A] =
    new Try2EitherSystemErrorImplicit(t)
}
