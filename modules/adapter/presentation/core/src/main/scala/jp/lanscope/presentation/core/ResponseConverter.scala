package jp.lanscope.presentation.core

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import io.circe.Encoder
import io.circe.Json._
import io.circe.syntax._
import jp.lanscope.domain._

import scala.jdk.CollectionConverters._

object ResponseConverter {
  protected val baseResponse: APIGatewayProxyResponseEvent = new APIGatewayProxyResponseEvent()
    .withIsBase64Encoded(false)
    .withHeaders(
      Map(
        "Access-Control-Allow-Origin" -> "*"
      ).asJava
    )

  protected def errorResponse(error: AnError): APIGatewayProxyResponseEvent =
    error match {
      case ValidationError(item, details, e) =>
        Logger(
          Level.Warn,
          message = s"ValidationError. item: ${item}. cause: ${e.getCause}. message: ${e.getMessage}",
          trace = e.getStackTrace.mkString("\n")
        ).print()
        baseResponse
          .withStatusCode(HttpStatusCode.BadRequest)
          .withBody(
            obj(
              "code" -> fromString("validation_error"),
              "details" -> arr(
                details.map(v =>
                  obj(
                    Seq(
                      Some("reason" -> fromString(v.reason)),
                      v.item.map(i => "item" -> { fromString(s"/$i") })
                      // TODO: 今の所、argsを返す箇所がない
                    ).flatten: _*
                  )
                ): _*
              )
            ).noSpaces
          )
      case NotFoundError(item, _) =>
        Logger(Level.Warn, message = s"NotFoundError. item: $item", trace = "").print()
        baseResponse
          .withStatusCode(HttpStatusCode.NotFound)
      case ExclusiveError(item, _) =>
        Logger(Level.Warn, message = s"ExclusiveError. item: $item", trace = "").print()
        baseResponse
          .withStatusCode(HttpStatusCode.Conflict)
      case SystemError(item, e) =>
        Logger(
          Level.Error,
          message = s"SystemError. item: $item. cause: ${e.getCause}. message: ${e.getMessage}",
          trace = e.getStackTrace.mkString("\n")
        ).print()
        baseResponse
          .withStatusCode(HttpStatusCode.InternalServerError)
      case e =>
        Logger(
          Level.Error,
          message = s"UnknownError. cause: ${e.getCause}. message: ${e.getMessage}",
          trace = e.getStackTrace.mkString("\n")
        ).print()
        baseResponse
          .withStatusCode(HttpStatusCode.InternalServerError)
    }

  implicit class ApiOptionalResponse[T](response: Either[AnError, Option[T]])(implicit encoder: Encoder[T]) {
    def toResponse: APIGatewayProxyResponseEvent =
      response.fold(
        error => errorResponse(error),
        {
          case None        => baseResponse.withStatusCode(HttpStatusCode.NoContent)
          case Some(value) => baseResponse.withStatusCode(HttpStatusCode.OK).withBody(value.asJson.noSpaces)
        }
      )
  }

  implicit class ApiResponse[T](response: Either[AnError, T])(implicit encoder: Encoder[T]) {
    def toResponse: APIGatewayProxyResponseEvent =
      response.fold(
        error => errorResponse(error),
        {
          case ()    => baseResponse.withStatusCode(HttpStatusCode.NoContent)
          case value => baseResponse.withStatusCode(HttpStatusCode.OK).withBody(value.asJson.noSpaces)
        }
      )
  }

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  implicit class ConsumerResponse[T](response: Either[AnError, T]) {
    def toResponse(input: Any): T =
      response.fold(
        {
          case error: SkipError[T] =>
            Logger(
              Level.Error,
              message = s"Task skipped. input: $input. message: ${error.getMessage}",
              trace = error.getStackTrace.mkString("\n")
            ).print()
            error.response
          case error =>
            Logger(
              Level.Error,
              message = s"input: $input. message: ${error.getMessage}",
              trace = error.getStackTrace.mkString("\n")
            ).print()
            throw new RuntimeException(error)
        },
        identity
      )
  }
}
