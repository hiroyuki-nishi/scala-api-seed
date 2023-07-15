//package jp.lanscope.infrastracture.dynamodb.auth
//
//import jp.lanscope.domain.Try2EitherSystemErrorImplicit.try2EitherSystemErrorImplicit
//import jp.lanscope.domain._
//import jp.lanscope.domain.auth._
//import software.amazon.awssdk.services.dynamodb.model.GetItemResponse
//
//import scala.jdk.CollectionConverters._
//import scala.util.{Failure, Success, Try}
//
//trait GroupACLRepositoryOnDynamoDB extends GroupACLRepository with DynamoDBWrapper {
//  protected val envName: Env
//  protected val tableName = s"auth_group_acl-${envName.value}"
//
//  val KeyGroupIds = "group_ids"
//
//  def getBy(companyId: CompanyId, personId: PersonId): Either[AnError, GroupACL] = {
//    def toGroupACL(response: GetItemResponse): Either[AnError, GroupACL] =
//      (for {
//        item <- Try(response.item())
//        groupACL <- Try(
//          GroupACL(
//            companyId = CompanyId(item.get(KeyCompanyId).s),
//            personId = PersonId(item.get(KeyPersonId).s),
//            version = Version(item.get(KeyVersion).n.toLong),
//            groupIds = item.get(KeyGroupIds).ss().asScala.toSeq.map(GroupId)
//          )
//        )
//      } yield groupACL).toEitherSystemError(s"toGroupACL in getBy tableName: $tableName, $companyId, $personId")
//
//    for {
//      response <- getItem(KeyCompanyId, companyId.value, KeyPersonId, personId.value) match {
//        case Success(r) if r.hasItem => Right(r)
//        case Success(_)              => Left(NotFoundError(s"getItem in getBy tableName: $tableName, $companyId, $personId", new RuntimeException))
//        case Failure(e)              => Left(SystemError(s"getItem in getBy tableName: $tableName, $companyId, $personId", e))
//      }
//      item <- toGroupACL(response)
//    } yield item
//  }
//}
