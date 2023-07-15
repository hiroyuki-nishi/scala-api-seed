package jp.lanscope.infrastracture.kinesisfirehose

import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
import software.amazon.awssdk.core.retry.RetryPolicy
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.firehose.FirehoseClient
import software.amazon.awssdk.services.firehose.model.{PutRecordBatchRequest, PutRecordBatchResponse, Record}

import scala.annotation.tailrec
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

trait KinesisFirehoseWrapper {
  protected val region: Region
  protected val streamName: String
  protected val BATCH_WRITE_FIREHOSE_MAX_COUNT: Int
  protected val RETRY_COUNT: Int

  protected val firehoseClient = FirehoseClient
    .builder()
    .region(region)
    .overrideConfiguration(
      ClientOverrideConfiguration
        .builder()
        .retryPolicy(RetryPolicy.builder().numRetries(RETRY_COUNT).build())
        .build()
    )
    .build()

  private def putRecordBatch(request: PutRecordBatchRequest.Builder): Try[PutRecordBatchResponse] =
    Try(firehoseClient.putRecordBatch(request.deliveryStreamName(streamName).build()))

  @tailrec
  private def internalPutRecords(records: Seq[Record]): Try[Unit] =
    (for {
      splitRecords <- Try(records.splitAt(BATCH_WRITE_FIREHOSE_MAX_COUNT))
      _            <- putRecordBatch(PutRecordBatchRequest.builder.records(splitRecords._1.asJava))
    } yield splitRecords._2) match {
      case Success(v) if v.nonEmpty =>
        internalPutRecords(v)
      case Success(_) => Success(())
      case Failure(e) => Failure(e)
    }

  def putRecords(data: Seq[Array[Byte]]): Try[Unit] =
    for {
      records <- Try(data.map(byte => Record.builder().data(SdkBytes.fromByteArray(byte)).build()))
      _ <- records match {
        case v if v.isEmpty => Success(())
        case v              => internalPutRecords(v)
      }
    } yield ()
}
