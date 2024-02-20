import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits.*
import skunk.Session
import skunk.implicits.*
import natchez.Trace.Implicits.noop
import pureconfig.{ConfigSource, loadConfigOrThrow}
import pureconfig.error.ConfigReaderFailures
import skunk_guide.db.UserRepository
import skunk_guide.domain.Config
import skunk_guide.modules.Resources

object main extends IOApp {

      override def run(args: List[String]): IO[ExitCode] =
        val config: Either[ConfigReaderFailures, Config] = ConfigSource.default.load[Config]
        config match
          case Left(configFailure) =>
            for {
              _ <- IO(println("Failed to load configurations"))
              _ <- IO(println(configFailure.toString))
            } yield ExitCode.Success
            
          case Right(configValues) =>
            val resource = new Resources[IO].single(configValues)
            for {
              userRepo <- UserRepository.make[IO](resource)
              _ <- IO(println("Creating users" + "_" * 50))
              johnId <- userRepo.create("John", "email@john.com")
              _ <- IO(println("John created with id: " + johnId))
              jacobId <- userRepo.create("Jacob", "email@jacob.com")
              _ <- IO(println("Jacob created with id: " + jacobId))
              kendrickId <- userRepo.create("Kendrick", "email@kendrick.com")
              _ <- IO(println("Kendrick created with id: " + kendrickId))
              _ <- IO(println("Fetching all users" + "_" * 50))
              users <- userRepo.findAll.compile.toList
              _ <- IO(println("Users found: " + users.toString()))
            } yield ExitCode.Success
}