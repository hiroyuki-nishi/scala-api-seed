package jp.lanscope.ctxname.presentation.consumer.xxx

import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage
import jp.lanscope.domain.AnError
import jp.lanscope.presentation.core.SqsRequestHandler

/**
  * SQSトリガーのLambdaサンプルコード
  */
trait Base extends SqsRequestHandler {

  override protected def messageReceived(message: SQSMessage): Either[AnError, Unit] = {
    for {
      // TODO: nishi cicreを使ったparseのサンプルコードを追加する
      _ <- Right("Hello World!")
    } yield ()
  }
}
