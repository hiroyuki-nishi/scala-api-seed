package jp.device.createdevice

import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import jp.lanscope.domain.device.DeviceRepository
import jp.lanscope.presentation.core.ApiHandler

trait Base extends ApiHandler {
  protected val deviceRepository: DeviceRepository
  override def handle(input: APIGatewayProxyRequestEvent): APIGatewayProxyResponseEvent = {
    import CreateDeviceRequest._
    import jp.lanscope.presentation.core.ResponseConverter.ApiResponse

    (for {
      request <- CreateDeviceRequest.decoder(input.getBody)
      device  <- request.create()
      _       <- deviceRepository.create(device)
    } yield device).toResponse // API側でJSON文字列にするためにimplicitでJsonProtocolを渡す必要がある
  }
}
