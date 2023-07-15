package jp.lanscope.domain

import java.time._
import java.time.format.DateTimeFormatter

object DateTimeUtil {
  protected val UtcZone: ZoneId                   = ZoneId.of("UTC")
  protected val JstZone: ZoneId                   = ZoneId.of("Asia/Tokyo")
  private val isoFormatter: DateTimeFormatter     = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  private val minutesFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
  private val dateFormatter: DateTimeFormatter    = DateTimeFormatter.ofPattern("yyyyMMdd")

  def now(): ZonedDateTime = ZonedDateTime.now(UtcZone)
  def jstDate(): LocalDate = ZonedDateTime.now(JstZone).toLocalDate

  def toISOString(date: ZonedDateTime): String        = date.format(isoFormatter)
  def toISOString(date: LocalDate): String            = date.format(dateFormatter)
  def toISOMinutesString(date: ZonedDateTime): String = date.format(minutesFormatter)

  def fromISOString(date: String): ZonedDateTime        = ZonedDateTime.parse(date).withZoneSameInstant(UtcZone)
  def fromISODateString(date: String): LocalDate        = LocalDate.parse(date, dateFormatter)
  def fromISOMinutesString(date: String): ZonedDateTime = ZonedDateTime.of(LocalDateTime.parse(date, minutesFormatter), UtcZone)

  def fromEpoch(date: BigDecimal): ZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli((date * 1000.0).toLong), UtcZone)
  def toEpoch(date: ZonedDateTime): BigDecimal   = date.toInstant.toEpochMilli / 1000.0
  def toInstant(date: ZonedDateTime): Instant    = date.toInstant

  implicit class LocalDateSyntax(value: LocalDate) {
    def <=(that: LocalDate): Boolean = value.isBefore(that) || value.isEqual(that)
  }
}
