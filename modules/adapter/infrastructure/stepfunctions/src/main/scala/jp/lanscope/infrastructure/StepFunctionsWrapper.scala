package jp.lanscope.infrastructure

import software.amazon.awssdk.services.sfn.SfnClient
import software.amazon.awssdk.services.sfn.model._

import scala.util.Try

trait StepFunctionsWrapper {
  protected val stateMachineArn: String
  protected val sfnClient: SfnClient

  protected def executeMachine(name: String, input: String): Try[StartExecutionResponse] =
    Try(
      sfnClient.startExecution(
        StartExecutionRequest
          .builder()
          .name(name)
          .stateMachineArn(stateMachineArn)
          .input(input)
          .build()
      )
    )

  protected def sendTaskSuccess(request: SendTaskSuccessRequest): Try[SendTaskSuccessResponse] =
    Try(sfnClient.sendTaskSuccess(request))

  protected def sendTaskFailure(request: SendTaskFailureRequest): Try[SendTaskFailureResponse] =
    Try(sfnClient.sendTaskFailure(request))
}
