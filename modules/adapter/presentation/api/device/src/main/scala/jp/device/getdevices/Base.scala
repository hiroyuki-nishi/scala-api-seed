package jp.device.getdevices

import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import jp.lanscope.domain.device.DeviceRepository
import jp.lanscope.domain.{AnError, CompanyId, ValidationError, ValidationErrorDetail}
import jp.lanscope.presentation.core.ApiHandler

import scala.util.Try

trait Base extends ApiHandler {
  protected val deviceRepository: DeviceRepository

  private def getCompanyId(input: APIGatewayProxyRequestEvent): Either[AnError, CompanyId] =
    Try(CompanyId(input.getQueryStringParameters.get("company_id"))).fold(
      e => Left(ValidationError("", Seq.empty[ValidationErrorDetail], e)),
      v => Right(v)
    )

  override def handle(input: APIGatewayProxyRequestEvent): APIGatewayProxyResponseEvent = {
    import Dto._
    import jp.lanscope.presentation.core.ResponseConverter.ApiResponse

    (for {
      companyId <- getCompanyId(input)
      device    <- deviceRepository.findAllBy(companyId)
    } yield device).toResponse // API側でJSON文字列にするためにimplicitでJsonProtocolを渡す必要がある
  }
}
