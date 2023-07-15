import sbt._

object Dependencies {
  // aws
  private lazy val awsSdkV2Version = "2.15.45"

  // circe
  private lazy val circeVersion = "0.12.3"
  private lazy val circeDependencies = Seq(
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion
  )

  // test
  private lazy val scalaTestVersion = "3.2.3"
  lazy val testDependencies = Seq(
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
    "org.scalatest" %% "scalatest-wordspec" % scalaTestVersion % Test,
    "org.scalamock" %% "scalamock" % "4.4.0" % Test
  )

  lazy val domainDependencies: Seq[ModuleID] = Seq()
  lazy val apiDependencies: Seq[ModuleID] = circeDependencies
  lazy val consumerDependencies: Seq[ModuleID] = circeDependencies
  lazy val senderDependencies: Seq[ModuleID] = circeDependencies

  lazy val dynamoDBDependencies: Seq[ModuleID] = Seq(
    "software.amazon.awssdk" % "dynamodb" % awsSdkV2Version
  )

  lazy val lambdaDependencies: Seq[ModuleID] = Seq(
    "com.amazonaws" % "aws-lambda-java-core" % "1.2.1",
    "com.amazonaws" % "aws-lambda-java-events" % "3.6.0"
  )

  lazy val s3Dependencies: Seq[ModuleID] = Seq(
    "software.amazon.awssdk" % "s3" % awsSdkV2Version
  )

  lazy val sqsDependencies: Seq[ModuleID] = Seq(
    "software.amazon.awssdk" % "sqs" % awsSdkV2Version
  )

  lazy val stepfunctionsDependencies: Seq[ModuleID] = Seq(
    "software.amazon.awssdk" % "sfn" % awsSdkV2Version
  )

  lazy val kinesisDependencies: Seq[ModuleID] = Seq(
    "software.amazon.awssdk" % "kinesis" % awsSdkV2Version
  )

  lazy val kinesisFirehoseDependencies: Seq[ModuleID] = Seq(
    "software.amazon.awssdk" % "firehose" % awsSdkV2Version
  )

  lazy val scalaLikeJdbcDependencies: Seq[ModuleID] = Seq(
    "org.scalikejdbc" %% "scalikejdbc"       % "3.5.0",
    "org.scalikejdbc" %% "scalikejdbc-config"  % "3.5.0",
    "mysql" % "mysql-connector-java" % "5.1.29" // mysqlに繋ぐために必要
  )

  lazy val macwireDependencies: Seq[ModuleID] = Seq(
    "com.softwaremill.macwire" %% "macros" % "2.4.0" % "provided",
    "com.softwaremill.macwire" %% "macrosakka" % "2.4.0" % "provided",
    "com.softwaremill.macwire" %% "util" % "2.4.0",
    "com.softwaremill.macwire" %% "proxy" % "2.4.0"
  )

  lazy val catsDependencies: Seq[ModuleID] = Seq(
    "org.typelevel" %% "cats-effect" % "3.2.2"
  )
}
