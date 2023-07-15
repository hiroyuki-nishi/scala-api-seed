package jp.lanscope.domain.auth

import jp.lanscope.domain.{CompanyId, GroupId, PersonId, Version}

case class GroupACL(
    companyId: CompanyId,
    personId: PersonId,
    version: Version,
    groupIds: Seq[GroupId]
)
