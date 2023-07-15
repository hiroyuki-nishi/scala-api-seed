name := "scala-seed"

import Dependencies._
import com.amazonaws.regions.{Region, Regions}

initialize := {
  val _ = initialize.value
  val requiredVersion = "11"
  val currentVersion = sys.props("java.specification.version")
  val vendor = sys.props("java.vendor")

  assert(
    "Amazon.com Inc." == sys.props("java.vendor"),
    s"異なるベンダーのJDKで実行されています。 Corretoで実行してください。 実行ベンダー: $vendor")
  assert(
    currentVersion == requiredVersion,
    s"異なるバージョンのJDKで実行されています。 JDK11で実行してください。 実行JDKバージョン: $currentVersion")
}

lazy val commonSettings = Seq(
  organization := "jp.lanscope",
  scalaVersion := "2.13.4",
  scalacOptions := Seq(
    "-Xfatal-warnings",
    "-deprecation",
    "-feature",
//    "-Wunused:locals",
    "-Wunused:imports"
//    "-Wunused:patvars",
//    "-Wunused:privates"
  ),
  scalacOptions in Test --= Seq(
    "-Ywarn-value-discard"
  ),
  scalafmtOnCompile in ThisBuild := true,
  test in assembly := {},
  libraryDependencies ++= testDependencies,
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
    case _ => MergeStrategy.first
  },
  assemblyJarName in assembly := s"${name.value}.jar",
  publishArtifact in(Compile, packageBin) := false,
  publishArtifact in(Compile, packageSrc) := false,
  publishArtifact in(Compile, packageDoc) := false,
  // wartremoverの設定
  wartremoverErrors in(Compile, compile) ++=
    Warts.allBut(
      Wart.DefaultArguments,
      Wart.Equals,
      Wart.FinalCaseClass,
      Wart.JavaSerializable,
      Wart.Nothing,
      Wart.Overloading,
      Wart.Serializable,
      Wart.StringPlusAny,
      Wart.NonUnitStatements
    )
)

lazy val ecrSettings = Seq(
  region           in Ecr := Region.getRegion(Regions.AP_NORTHEAST_1),
  repositoryName   in Ecr := "sample/hello-world",
  localDockerImage in Ecr := (packageName in Docker).value + ":" + (version in Docker).value,
  repositoryTags   in Ecr ++= Seq(version.value), // タグを付ける場合は指定する
  // Authenticate and publish a local Docker image before pushing to ECR,
  push in Ecr := ((push in Ecr) dependsOn (publishLocal in Docker, login in Ecr)).value,
)

lazy val root = (project in file("."))
  .aggregate(
    presentation,
    domain,
    sender,
    infrastructure
  )
  .settings(commonSettings: _*)
  .settings(
    commonSettings,
    publishArtifact := false
  )
  .settings(
    commands += Command.command("assemblyAll") { state =>
      "sampleLambda / assembly" ::
        state
    }
  )

//** domain **//
lazy val domain = (project in file("modules/domain"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= domainDependencies ++ catsDependencies
  )


//** presentation **//
lazy val presentation = (project in file("modules/adapter/presentation"))
  .aggregate(
    api,
    consumer,
    presentationCore
  )
  .settings(
    name := "scala-seed-presentation",
    publishArtifact := false
  )

lazy val api = (project in file("modules/adapter/presentation/api"))
  .dependsOn(domain)
  .aggregate(deviceApi)
  .settings(commonSettings: _*)

lazy val consumer = (project in file("modules/adapter/presentation/consumer"))
  .dependsOn(domain)
  .aggregate(sampleConsumer)
  .settings(commonSettings: _*)

lazy val presentationCore = (project in file("modules/adapter/presentation/core"))
  .dependsOn(domain, sender)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= lambdaDependencies
  )

lazy val deviceApi = (project in file("modules/adapter/presentation/api/device"))
  .dependsOn(domain, presentationCore, infraDynamoDB)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= macwireDependencies ++ catsDependencies
  )

lazy val sampleConsumer = (project in file("modules/adapter/presentation/consumer/sampleconsumer"))
  .enablePlugins(EcrPlugin, JavaAppPackaging, AshScriptPlugin, DockerPlugin)
  .dependsOn(domain, presentationCore)
  .settings(commonSettings: _*)
  .settings(ecrSettings: _*)
  .settings(
//    organization := "io.github.atty303",
//    name := "example01",
    version := version.value,
    mainClass in assembly := Some("sample.fargate.Main"),
    dockerBaseImage := "amazoncorretto:8-alpine-jre"
  )

/** sender
 *
 * */
lazy val sender = (project in file("modules/sender"))
  .dependsOn(domain)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= senderDependencies
  )


/** infrastructure
 *
 * */
lazy val infrastructure = (project in file("modules/adapter/infrastructure"))
  .settings(commonSettings: _*)
  .aggregate(
    infraS3,
    infraSQS,
    infraStepFunctions,
    infraKinesis,
    infraKinesisFirehose
  )

lazy val infraDynamoDB = (project in file("modules/adapter/infrastructure/dynamodb"))
  .dependsOn(domain)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= dynamoDBDependencies ++ catsDependencies,
    parallelExecution in Test := false
  )

lazy val infraS3 = (project in file("modules/adapter/infrastructure/s3"))
  .dependsOn(domain)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= s3Dependencies,
    parallelExecution in Test := false
  )

lazy val infraSQS = (project in file("modules/adapter/infrastructure/sqs"))
  .dependsOn(domain)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= sqsDependencies,
    parallelExecution in Test := false
  )

lazy val infraStepFunctions = (project in file("modules/adapter/infrastructure/stepfunctions"))
  .dependsOn(domain)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= stepfunctionsDependencies,
    parallelExecution in Test := false
  )

lazy val infraKinesis = (project in file("modules/adapter/infrastructure/kinesis"))
  .dependsOn(domain)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= kinesisDependencies,
    parallelExecution in Test := false
  )

lazy val infraKinesisFirehose = (project in file("modules/adapter/infrastructure/kinesisfirehose"))
  .dependsOn(domain)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= kinesisFirehoseDependencies,
    parallelExecution in Test := false
  )

lazy val infraRds = (project in file("modules/adapter/infrastructure/rds"))
  .dependsOn(domain)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= scalaLikeJdbcDependencies,
    parallelExecution in Test := false
  )
