package jp.sample.day1
import scala.language.implicitConversions

trait Monoid[A] {
  def mappend(a: A, b: A): A
  def mzero: A
}
object Monoid {
  object syntax extends MonoidSyntax

  implicit val IntMonoid: Monoid[Int] = new Monoid[Int] {
    def mappend(a: Int, b: Int): Int = a + b
    def mzero: Int                   = 0
  }
  implicit val StringMonoid: Monoid[String] = new Monoid[String] {
    def mappend(a: String, b: String): String = a + b
    def mzero: String                         = ""
  }
}
trait MonoidSyntax {
  implicit final def syntaxMonoid[A: Monoid](a: A): MonoidOps[A] =
    new MonoidOps[A](a)
}
final class MonoidOps[A: Monoid](lhs: A) {
  def |+|(rhs: A): A     = implicitly[Monoid[A]].mappend(lhs, rhs)
  def hoge(rhs: A): Unit = println(rhs)
}

object App extends App {
  import Monoid.syntax._
  println(3 |+| 4)
  println("a" |+| "b")
  3.hoge(4)
}
