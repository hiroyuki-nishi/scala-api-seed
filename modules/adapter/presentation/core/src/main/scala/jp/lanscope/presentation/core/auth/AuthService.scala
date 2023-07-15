package jp.lanscope.presentation.core.auth

import jp.lanscope.domain.auth.GroupACLRepository
import jp.lanscope.domain._

trait AuthService {
  protected val groupACLRepository: GroupACLRepository

  def authorize(companyId: CompanyId, personId: PersonId, targetGroupId: GroupId): Either[AnError, Unit] = {
    groupACLRepository.findBy(companyId, personId) match {
      case Right(groupACL) if groupACL.groupIds.exists(g => g.value == targetGroupId.value) => Right(())
      case Right(_)                                                                         => Left(NotFoundError(s"authorize in AuthService: $companyId, $personId, $targetGroupId", new RuntimeException("")))
      case Left(e)                                                                          => Left(e)
    }
  }

  def getAuthorizedGroupIds(companyId: CompanyId, personId: PersonId): Either[AnError, Seq[GroupId]] = {
    groupACLRepository.findBy(companyId, personId) match {
      case Right(groupACL) if groupACL.groupIds.nonEmpty => Right(groupACL.groupIds)
      case Right(_)                                      => Left(NotFoundError(s"getAuthorizedGroupIds in AuthService: $companyId, $personId", new RuntimeException("")))
      case Left(e)                                       => Left(e)
    }
  }
}
