package jp.lanscope.ctxname.presentation.api.xxx

import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import io.circe.ParsingFailure
import io.circe.parser.decode
import jp.lanscope.domain.{AnError, SystemError, ValidationError, ValidationErrorIllegalValue}
import jp.lanscope.presentation.core.ApiHandler

import JsonProtocol._

import scala.util.Try

trait Base extends ApiHandler {
  private def getBody(input: APIGatewayProxyRequestEvent): Either[AnError, String] = {
    Try(input.getBody).fold(
      error => Left(SystemError("", error.fillInStackTrace())),
      v => Right(v)
    )
  }
  protected def parseRequest(body: String): Either[AnError, SampleAuthRequest] = {
    decode[SampleAuthRequest](body).fold(
      {
        case e: ParsingFailure => Left(ValidationError(body, Seq(ValidationErrorIllegalValue(Some(""), None)), e))
        case error             => Left(SystemError(body, error.fillInStackTrace()))
      },
      Right(_)
    )
  }

  override def handle(input: APIGatewayProxyRequestEvent): APIGatewayProxyResponseEvent = {
    import jp.lanscope.presentation.core.ResponseConverter.ApiResponse
    (for {
      body <- getBody(input)
      v    <- parseRequest(body)
    } yield v).toResponse // API側でJSON文字列にするためにimplicitでJsonProtocolを渡す必要がある
  }
}
