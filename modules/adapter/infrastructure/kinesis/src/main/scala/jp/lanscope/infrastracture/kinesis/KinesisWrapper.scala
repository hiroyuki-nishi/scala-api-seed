package jp.lanscope.infrastracture.kinesis

import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.kinesis.KinesisClient
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest

import scala.util.Try

case class SequenceNumber(value: String) extends AnyVal

trait KinesisWrapper {
  protected val region: Region
  protected val streamName: String
  protected val kinesisClient: KinesisClient = KinesisClient
    .builder()
    .region(region)
    .build()

  protected def putItem(partitionKey: String, data: Array[Byte]): Try[SequenceNumber] =
    for {
      request <- Try(
        PutRecordRequest
          .builder()
          .partitionKey(partitionKey)
          .data(SdkBytes.fromByteArray(data))
      )
      response <- Try(kinesisClient.putRecord(request.streamName(streamName).build()))
    } yield SequenceNumber(response.sequenceNumber())
}
