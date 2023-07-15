package jp.lanscope.domain.auth

import jp.lanscope.domain.{AnError, CompanyId, PersonId}

trait GroupACLRepository {
  def findBy(companyId: CompanyId, personId: PersonId): Either[AnError, GroupACL]
}
