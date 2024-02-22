package playground

import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.rockthejvm.domain.Config
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures
import playground.DbConnect
import skunk.*
import skunk.implicits.*
import skunk.codec.all.*
import natchez.Trace.Implicits.noop

/**
  * Created by Bomen Derick.
  */
object main extends IOApp {

  def singleSession(config: Config): IO[Unit] = DbConnect.single[IO](config).use { session =>
    for {
      _           <- IO(println("Using a single session..."))
      dateAndTime <- session.unique(sql"select current_timestamp".query(timestamptz))
      _           <- IO(println(s"Current date and time is $dateAndTime."))
    } yield ()
  }

  def pooledSession(config: Config): IO[Unit] = DbConnect.pooled[IO](config).use { resource =>
    resource.use { session =>
      for {
        _           <- IO(println("Using a pooled session..."))
        dateAndTime <- session.unique(sql"select current_timestamp".query(timestamptz))
        _           <- IO(println(s"Current date and time is $dateAndTime."))
      } yield ()
    }
  }

  override def run(args: List[String]): IO[ExitCode] =
    val config: Either[ConfigReaderFailures, Config] = ConfigSource.default.at("db").load[Config]
    config match
      case Left(configFailure) =>
        for {
          _ <- IO(println(configFailure.prettyPrint(2)))
        } yield ExitCode.Success

      case Right(configValues) =>
        singleSession(configValues) *> pooledSession(configValues) *> IO.pure(ExitCode.Success)

}
