package jp.lanscope.presentation.core

import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage
import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent, KinesisEvent, SQSEvent}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import jp.lanscope.domain.AnError

import scala.jdk.CollectionConverters._
import scala.util.Try

trait ApiHandler extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] {
  protected def log(input: APIGatewayProxyRequestEvent): Unit =
    Try {
      val globalIp = Try(input.getHeaders.get("X-Forwarded-For")).getOrElse("")
      Logger(Level.Info, globalIp = Some(globalIp), message = input.toString, trace = "")
    }.fold(
      e => {
        Logger(Level.Error, message = "ERROR: log()", trace = e.toString).print()
      },
      v => v.print()
    )

  protected def handle(input: APIGatewayProxyRequestEvent): APIGatewayProxyResponseEvent

  override protected def handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {
    log(input)
    handle(input)
  }
}

trait SqsRequestHandler extends RequestHandler[SQSEvent, Unit] {
  protected def messageReceived(message: SQSMessage): Either[AnError, Unit]

  protected def log(input: SQSEvent): Unit =
    Try(Logger(Level.Info, message = input.toString, trace = ""))
      .fold(
        e => Logger(Level.Error, message = "ERROR: log()", trace = e.toString).print(),
        v => v.print()
      )

  override protected def handleRequest(input: SQSEvent, context: Context): Unit = {
    import jp.lanscope.presentation.core.ResponseConverter._
    log(input)
    input.getRecords.asScala.foreach(message => messageReceived(message).toResponse(input))
  }
}

trait KinesisRequestHandler extends RequestHandler[KinesisEvent, Unit] {
  protected def recordReceived(record: KinesisEventRecord): Either[AnError, Unit]

  protected def log(input: KinesisEvent): Unit =
    Try(Logger(Level.Info, message = input.toString, trace = ""))
      .fold(
        e => Logger(Level.Error, message = "ERROR: log()", trace = e.toString).print(),
        v => v.print()
      )

  override protected def handleRequest(input: KinesisEvent, context: Context): Unit = {
    import jp.lanscope.presentation.core.ResponseConverter._
    log(input)
    input.getRecords.asScala.foreach(record => recordReceived(record).toResponse(input))
  }
}
