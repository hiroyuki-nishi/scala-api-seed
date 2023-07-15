package jp.sample.day1

import cats.effect.{IO, Ref}

object RefApp extends App {

  def e1: IO[Ref[IO, Int]] =
    for {
      r <- Ref[IO].of(0)
      _ <- r.update(_ + 1)
    } yield r

  def e2: IO[Int] =
    for {
      r <- e1
      x <- r.get
      _ = println(x)
    } yield x

  {
    import cats.effect.unsafe.implicits._
    e2.unsafeRunSync()
  }

}
