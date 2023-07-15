package jp.lanscope.inrastructure.sqs

import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model._

import scala.jdk.CollectionConverters._
import scala.util.Try

trait SqsWrapper {
  type SqsMessage = Message
  case class SqsResponse(receiptHandle: Option[String], messages: Option[Seq[SqsMessage]])
  object SqsResponse {
    def create(r: ReceiveMessageResponse): SqsResponse = {
      val maybeHandle = r.messages().asScala.headOption match {
        case None    => None
        case Some(v) => Some(v.receiptHandle())
      }
      val messages = r.messages().asScala.toSeq
      SqsResponse(
        maybeHandle,
        if (messages.nonEmpty) Some(messages) else None
      )
    }
  }

  protected val sqsClient: SqsClient
  protected val queueUrl: String

  protected def receiveMessage(count: Int): Try[SqsResponse] =
    for {
      response <- Try(
        sqsClient.receiveMessage(
          ReceiveMessageRequest
            .builder()
            .maxNumberOfMessages(count)
            .queueUrl(queueUrl)
            .build()
        )
      )
    } yield SqsResponse.create(response)

  protected def sendMessage(messageBody: String): Try[String] =
    for {
      request <- Try(
        SendMessageRequest
          .builder()
          .queueUrl(queueUrl)
          .messageBody(messageBody)
          .build()
      )
      _ <- Try(sqsClient.sendMessage(request))
    } yield messageBody

  protected def deleteMessage(receiptHandle: String): Try[Unit] = {
    for {
      request <- Try(
        DeleteMessageRequest
          .builder()
          .queueUrl(queueUrl)
          .receiptHandle(receiptHandle)
          .build()
      )
      _ <- Try(sqsClient.deleteMessage(request))
    } yield ()
  }
}
