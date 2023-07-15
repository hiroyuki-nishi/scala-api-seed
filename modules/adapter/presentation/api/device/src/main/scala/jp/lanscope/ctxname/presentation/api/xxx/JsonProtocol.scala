package jp.lanscope.ctxname.presentation.api.xxx

import io.circe.Json.{fromString, obj}
import io.circe.{Decoder, Encoder, HCursor}
import jp.lanscope.domain.{AccountId, CompanyId, PersonId}

final case class SampleAuthRequest(companyId: CompanyId, accountId: AccountId, personId: PersonId)

object JsonProtocol {
  implicit val encoder: Encoder[SampleAuthRequest] = (b: SampleAuthRequest) => {
    obj(
      Seq(
        Some("company_id" -> fromString(b.companyId.value)),
        Some("account_id" -> fromString(b.accountId.value)),
        Some("person_id"  -> fromString(b.personId.value))
      ).flatten: _*
    )
  }

  implicit val decoder: Decoder[SampleAuthRequest] = (c: HCursor) =>
    for {
      companyId <- c.downField("company_id").as[String]
      accountId <- c.downField("account_id").as[String]
      personId  <- c.downField("person_id").as[String]
    } yield SampleAuthRequest(
      CompanyId(companyId),
      AccountId(accountId),
      PersonId(personId)
    )
}
