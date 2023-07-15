package jp.device.getdevices

import org.scalamock.matchers.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.diagrams.Diagrams
import org.scalatest.wordspec.AnyWordSpec

class DtoSpec extends AnyWordSpec with GivenWhenThen with Matchers with MockFactory with Diagrams {

  "DevicesRequest" when {
    "正常なリクエストが送信された時" should {
      "200が返される" in new WithFixture {}
    }
  }

  trait WithFixture {}
}
