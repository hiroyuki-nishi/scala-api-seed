package jp.sample

import com.softwaremill.macwire._
import org.scalamock.matchers.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.diagrams.Diagrams
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Try

trait CompanyRepository {
  val companyName: String
  def findAll: Try[Seq[String]]
}
trait UserRepository {
  def findAll: Try[Seq[String]]
}

class CompanyRepositoryImpl(name: String) extends CompanyRepository {
  override val companyName: String       = name
  override def findAll: Try[Seq[String]] = Try(Seq(companyName))
}

class UserRepositoryImpl extends UserRepository {
  override def findAll: Try[Seq[String]] = Try(Seq("A"))
}

class UserAddUseCase(companyRepository: CompanyRepository, userRepository: UserRepository) {
  def findAll: Seq[String] = {
    (for {
      c <- companyRepository.findAll
      u <- userRepository.findAll
    } yield c).fold(
      e => throw e,
      v => v
    )
  }
}

class AppSpec extends AnyWordSpec with GivenWhenThen with Matchers with MockFactory with Diagrams {
  "App" when {
    "macwire Dependency Injection" should {
      "DI成功" in new WithModule {
        private lazy val userAddUseCase: UserAddUseCase = wire[UserAddUseCase]
        private val actual                              = userAddUseCase.findAll
        actual.head should be("COMPANY_A")
      }
    }

    "Manual Dependency Injection" should {
      "DI成功" in {
        val userAddUseCase = new UserAddUseCase(new CompanyRepositoryImpl(""), new UserRepositoryImpl())
        val actual         = userAddUseCase.findAll
      }
    }
  }

  trait WithModule {
    lazy val c = new CompanyRepositoryImpl("COMPANY_A")
//    lazy val c = wire[CompanyRepositoryImpl]
    lazy val u = wire[UserRepositoryImpl]
  }
}
