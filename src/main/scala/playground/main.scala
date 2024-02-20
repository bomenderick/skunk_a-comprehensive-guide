package playground

import cats.effect.{ExitCode, IO, IOApp}
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures
import skunk_guide.domain.Config
import playground.Sessions
import skunk.*
import skunk.implicits.*
import skunk.codec.all.*
import natchez.Trace.Implicits.noop

/**
  * Created by Bomen Derick.
  */
object main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    val config: Either[ConfigReaderFailures, Config] = ConfigSource.default.load[Config]
    config match
      case Left(configFailure) =>
        for {
          _ <- IO(println(configFailure.prettyPrint(2)))
        } yield ExitCode.Success

      case Right(configValues) =>
        val resource = new Sessions[IO].single(configValues)
        resource.use { session =>
          for {
            dateAndTime <- session.unique(sql"select current_timestamp".query(timestamptz))
            _           <- IO(println(s"Current date and time is $dateAndTime."))
          } yield ExitCode.Success
        }

}
