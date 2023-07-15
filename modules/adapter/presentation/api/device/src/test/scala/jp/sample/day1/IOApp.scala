package jp.sample.day1

import cats.effect._

import java.io.{BufferedReader, File, FileReader}
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object IOApp extends App {
  import cats.effect.unsafe.implicits.global

  (for {
    _ <- IO.print("What's your name?")
    x <- IO.readLine
    _ <- IO.println(s"Hello, $x")
  } yield ()).unsafeRunSync()
}

object IOErrorApp extends App {
  import cats.effect.unsafe.implicits.global

  private val io    = IO("Hello World!")
  private val error = IO(throw new RuntimeException("error"))

  private val r = (for {
    a <- io
    _ <- error
  } yield a)

  println(r.attempt.unsafeRunSync())
  println(
    r.handleErrorWith { error =>
      IO.pure(error.getMessage)
    }.unsafeRunSync()
  )
}

object IOReadApp extends App {
  def readFirstLine(file: File): IO[String] = {
    IO(new BufferedReader(new FileReader((file)))).bracket { in =>
      println(in.readLine())
      IO(in.readLine())
    } { in =>
      IO(in.close())
    }
  }

  private lazy val file = new File("./test.txt")
  readFirstLine(file)
}

object IOAsyncApp extends App {
  import cats.effect.unsafe.implicits.global
  private val task = IO {
    println(s"Thread: ${Thread.currentThread.getName}")
    println("task")
  }

  private val ec1 = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
  private val ec2 = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
  private val ec3 = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))

  val r  = task >> task >> task
  val r2 = task.evalOn(ec1) >> task.evalOn(ec2) >> task.evalOn(ec3)
  r.unsafeRunSync()
  r2.unsafeRunSync()
}

object OtherThreadApp extends App {
  import cats.effect.unsafe.implicits.global

  private val cachedThreadPool                  = Executors.newCachedThreadPool()
  private val executionContextForBlockingFileIO = ExecutionContext.fromExecutor(cachedThreadPool)
  private val mainExectuionContext              = ExecutionContext.global

  private val task = (v: Int, name: String) =>
    IO {
      println(s"Thread: ${Thread.currentThread.getName}")
      println(s"Task Name: $name")
      Thread.sleep(v)
      name
    }

  private val ioa: IO[Unit] =
    for {
      // 同期処理
      _ <- task(1000, "task1").evalOn(executionContextForBlockingFileIO)
      _ <- task(500, "task2").evalOn(mainExectuionContext)
      // 非同期処理
      a <- task(4000, "task3").evalOn(executionContextForBlockingFileIO).start
      b <- task(500, "task4").evalOn(mainExectuionContext).start
      // bの処理が終わったら進む
      b1 <- b.join
      a1 <- a.join
      _ = println(a1)
      _ = println(b1)
      _ <- IO(cachedThreadPool.shutdown())
    } yield ()

  ioa.unsafeRunSync()
}

object StartSyncApp extends App {
  import cats.effect.unsafe.implicits.global

  private val start = System.currentTimeMillis
  private val task = (v: Int, name: String) =>
    IO {
      println(s"Thread: ${Thread.currentThread.getName}")
      println(s"Task Name: $name")
      Thread.sleep(v)
      name
    }

  (for {
    // 同期実行
    _ <- task(5000, "task1")
    _ <- task(1000, "task2")
  } yield (println("処理時間： " + (System.currentTimeMillis - start) + " ミリ秒")))
    .unsafeRunSync()
}

object StartAsyncApp extends App {
  import cats.effect.unsafe.implicits.global

  private val task = (v: Int, name: String) =>
    IO {
      println(s"Thread: ${Thread.currentThread.getName}")
      println(s"Task Name: $name")
      Thread.sleep(v)
      name
    }

  private val start = System.currentTimeMillis
  (for {
    f1 <- task(3000, "task1").start // ブロックされない処理
    f2 <- task(1000, "task2").start
    _  <- f1.join // 処理結果を取得
    _  <- f2.join
  } yield (println("処理時間： " + (System.currentTimeMillis - start) + " ミリ秒")))
    .unsafeRunSync()
}

object IOCedeApp extends App {
  import cats.effect.unsafe.implicits.global

  private val task = IO {
    println(s"Thread: ${Thread.currentThread.getName}")
    println("task")
  }

  private val ec1 = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
//  private val ec2 = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
//  private val ec3 = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
//
  (for {
    _ <- task
    _ <- task
  } yield ()).unsafeRunSync()
}
