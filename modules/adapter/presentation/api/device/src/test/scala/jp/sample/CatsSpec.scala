package jp.sample
import cats.implicits._
import org.scalamock.matchers.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.diagrams.Diagrams
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class CatsSpec extends AnyWordSpec with GivenWhenThen with Matchers with MockFactory with Diagrams {

  "Test" when {
    "test" should {
      "200が返される" in new WithFixture {
        When("処理実行")
        val success1 = "a".asRight[Int]
        val success2 = "b".asRight[Int]
        val failure  = 400.asLeft[String]

        success1 *> success2
        println(success1)
        println(success1 *> success2)
        println(success1 *> failure)
        // Some[String]

        val a = "a".some
        // Option[String]
        print(a)
        ("a".some, "b".some).mapN(_ ++ _)
        // Some(ab)

        Then("200 OKが返される")
        1 shouldBe (1)
      }
    }
  }

  trait WithFixture {}
}
