package jp.lanscope.infrastructure.s3

import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model._
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.{GetObjectPresignRequest, UploadPartPresignRequest}

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import scala.annotation.tailrec
import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters._
import scala.jdk.DurationConverters._
import scala.util.{Failure, Success, Try}

case class Part(partNumber: Int, eTag: String)

trait S3Wrapper {
  type AwsS3Object = S3Object
  protected val bucketName: String
  protected val s3Client: S3Client
  protected val s3Presigner: S3Presigner

  protected def findObject(key: String): Try[Option[Array[Byte]]] =
    (for {
      request <- Try(
        GetObjectRequest
          .builder()
          .key(key)
          .bucket(bucketName)
          .build()
      )
      response <- Try(s3Client.getObjectAsBytes(request))
    } yield response.asByteArray()).fold(
      e => Failure(e),
      {
        case v if v.nonEmpty => Success(Some(v))
        case _               => Success(None)
      }
    )

  protected def putObject(key: String, bytes: Array[Byte]): Try[Unit] =
    for {
      _ <- Try(
        s3Client.putObject(
          PutObjectRequest
            .builder()
            .key(key)
            .bucket(bucketName)
            .build(),
          RequestBody.fromBytes(bytes)
        )
      )
    } yield ()

  protected def deleteObject(key: String): Try[Unit] =
    for {
      _ <- Try(
        s3Client.deleteObject(
          DeleteObjectRequest
            .builder()
            .key(key)
            .bucket(bucketName)
            .build()
        )
      )
    } yield ()

  @tailrec
  private def findAllInternal(acc: Seq[S3Object], marker: Option[String]): Try[Seq[S3Object]] = {
    def createListObjectsRequest(marker: Option[String]): Try[ListObjectsRequest] =
      Try {
        val builder = ListObjectsRequest.builder().bucket(bucketName)
        if (marker.isDefined) builder.marker(marker.getOrElse("")).build() else builder.build()
      }

    (for {
      request <- createListObjectsRequest(marker)
      res     <- Try(s3Client.listObjects(request))
    } yield res) match {
      case Success(response: ListObjectsResponse) =>
        val accS3Objects = acc ++ response.contents().asScala.toSeq
        if (response.isTruncated) {
          findAllInternal(accS3Objects, Some(response.contents().asScala.toArray[S3Object].last.key()))
        } else {
          Success(accS3Objects)
        }
      case Failure(e) => Failure(e)
    }
  }

  protected def listObjects(): Try[Option[Seq[AwsS3Object]]] = {
    (for {
      response <- findAllInternal(Seq.empty[S3Object], None)
    } yield response).fold(
      e => Failure(e),
      {
        case v if v.nonEmpty => Success(Some(v))
        case _               => Success(None)
      }
    )
  }

  protected def createPreSignedUrl(key: String, fileName: String, expiration: FiniteDuration): Try[String] =
    for {
      name <- Try(URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString))
      request <- Try(
        GetObjectPresignRequest
          .builder()
          .signatureDuration(expiration.toJava)
          .getObjectRequest(
            GetObjectRequest
              .builder()
              .key(key)
              .bucket(bucketName)
              .responseContentDisposition(s"attachment;filename=$name;filename*=UTF-8''$name")
              .build()
          )
          .build()
      )
      url <- Try(s3Presigner.presignGetObject(request).url().toString)
    } yield url

  protected def createMultiPartUploadId(key: String, expiration: Instant): Try[String] =
    for {
      request <- Try(
        CreateMultipartUploadRequest
          .builder()
          .bucket(bucketName)
          .key(key)
          .expires(expiration)
          .build()
      )
      uploadId <- Try(s3Client.createMultipartUpload(request).uploadId())
    } yield uploadId

  protected def createMultiPartPreSignedUrl(key: String, uploadId: String, partNumber: Int, expiration: FiniteDuration): Try[String] =
    for {
      request <- Try(
        UploadPartPresignRequest
          .builder()
          .signatureDuration(expiration.toJava)
          .uploadPartRequest(
            UploadPartRequest
              .builder()
              .bucket(bucketName)
              .key(key)
              .uploadId(uploadId)
              .partNumber(partNumber)
              .build()
          )
          .build()
      )
      url <- Try(s3Presigner.presignUploadPart(request).url().toString)
    } yield url

  private def convertCompleteParts(parts: Seq[Part]): Try[Seq[CompletedPart]] =
    Try(parts.map(x => CompletedPart.builder().partNumber(x.partNumber).eTag(x.eTag).build()))

  protected def completedMultipart(key: String, uploadId: String, parts: Seq[Part]): Try[Unit] =
    for {
      completeParts <- convertCompleteParts(parts)
      completedMultipartUpload <- Try(
        CompletedMultipartUpload
          .builder()
          .parts(completeParts.asJava)
          .build()
      )
      request <- Try(
        CompleteMultipartUploadRequest
          .builder()
          .bucket(bucketName)
          .key(key)
          .uploadId(uploadId)
          .multipartUpload(completedMultipartUpload)
          .build()
      )
      _ <- Try(s3Client.completeMultipartUpload(request))
    } yield ()
}
