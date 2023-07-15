package jp.lanscope.presentation.core

import jp.lanscope.domain.{ClientId, CompanyId, DateTimeUtil, DeviceId, Enum, Enums}

/**
  *   @param timestamp: RFC3339準拠 UTC
  *   @param level: ERROR,WARN,INFO,DEBUG,TRACE
  *   @param context: リポジトリ名
  *   @param application: 処理名、Lambda名など
  *   @param companyId: companyId
  *   @param clientId: clientId
  *   @param deviceId: deviceId
  *   @param specifiedId: 上記id以外に出力必要なものがあれば
  *   @param message: 開発者が任意でセットするメッセージ
  *   @param trace: スタックトレース
  */

sealed abstract class Level(val value: String) extends Enum
object Level extends Enums[Level] {
  case object Error extends Level("ERROR")
  case object Warn  extends Level("WARN")
  case object Info  extends Level("INFO")
  case object Debug extends Level("DEBUG")
  case object Trace extends Level("TRACE")
  override val values: Set[Level] = Set(
    Error,
    Warn,
    Info,
    Debug,
    Trace
  )
}

class Logger(
    val timestamp: String,
    val level: Level,
    val context: String,
    val application: String,
    val companyId: Option[CompanyId],
    val clientId: Option[ClientId],
    val deviceId: Option[DeviceId],
    val specifiedId: Option[String],
    val globalIp: Option[String],
    val message: String,
    val trace: String
) {
  def print(): Unit =
    println(
      s"{ level: ${level.value}, timestamp: $timestamp, context: $context, application: $application, company_id: $companyId, client_id: $clientId, device_id: $deviceId, specified_id: $specifiedId, global_ip: $globalIp, message: $message, trace: $trace }"
    )
}

object Logger {
  def apply(
      level: Level,
      companyId: Option[CompanyId] = None, // TODO: nishi
      clientId: Option[ClientId] = None,
      deviceId: Option[DeviceId] = None,
      specifiedId: Option[String] = None,
      globalIp: Option[String] = None,
      message: String,
      trace: String
  ): Logger =
    new Logger(
      DateTimeUtil.toISOString(DateTimeUtil.now()),
      level,
      sys.env.getOrElse("context", "unknown"),
      sys.env.getOrElse("application", "unknown"),
      companyId,
      clientId,
      deviceId,
      specifiedId,
      globalIp,
      message,
      trace
    )
}
