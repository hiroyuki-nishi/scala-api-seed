import cats._, cats.syntax.all._

1 === 1
1 =!= 2
//1 === "foo"
1 == "foo"

(Some(1): Option[Int]) =!= (Some(1): Option[Int])
