package jp.sample.day1

import java.util.concurrent.Executors
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

object FutureApp2 extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  private def task(name: String, milliSeconds: Int) =
    Future {
      println(s"Thread: ${Thread.currentThread.getName}")
      println(s"Task Name: $name")
      Thread.sleep(milliSeconds)
      name
    }

  private val start = System.currentTimeMillis
  private val result = for {
    _ <- task("task1", 1000)
    _ <- task("task2", 1000)
  } yield ()

  Await.ready(result, Duration.Inf)
  result onComplete {
    case Success(_) => println("処理時間： " + (System.currentTimeMillis - start) + " ミリ秒")
    case Failure(e) => println("エラーが発生した: " + e.getMessage)
  }
}

object FutureOutApp extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  private def task(name: String, milliSeconds: Int) =
    Future {
      println(s"Thread: ${Thread.currentThread.getName}")
      println(s"Task Name: $name")
      Thread.sleep(milliSeconds)
      name
    }

  private val task1 = task("task1", 1000)
  private val task2 = task("task2", 1000)

  private val start = System.currentTimeMillis
  private val result = for {
    _ <- task1
    _ <- task2
  } yield ()

  Await.ready(result, Duration.Inf)
  result onComplete {
    case Success(_) => println("処理時間： " + (System.currentTimeMillis - start) + " ミリ秒")
    case Failure(e) => println("エラーが発生した: " + e.getMessage)
  }
}

object FutureApp extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  private val result = for {
    s <- Future("hello")
  } yield s

  Await.ready(result, Duration.Inf)
  result onComplete {
    case Success(s) => println(s)
    case Failure(e) => println("エラーが発生した: " + e.getMessage)
  }
}

object FutureBlockingApp extends App {
  private implicit val blockingContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))

  private val result = for {
    s <- Future("hello")
  } yield s

  Await.ready(result, Duration.Inf)
  result onComplete {
    case Success(s) => println(s)
    case Failure(e) => println("エラーが発生した: " + e.getMessage)
  }
}
