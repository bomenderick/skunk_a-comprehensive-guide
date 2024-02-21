import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits.*
import skunk.Session
import skunk.implicits.*
import natchez.Trace.Implicits.noop
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures
import skunk_guide.db.UserRepository
import skunk_guide.domain.{Config, User}
import skunk_guide.modules.DbConnection

object main extends IOApp {

      override def run(args: List[String]): IO[ExitCode] =
        val config: Either[ConfigReaderFailures, Config] = ConfigSource.default.load[Config]
        config match
          case Left(configFailure) =>
            for {
              _ <- IO(println("Failed to load configurations"))
              _ <- IO(println(configFailure.prettyPrint()))
            } yield ExitCode.Success

          case Right(configValues) =>
            val resource = new DbConnection[IO].single(configValues)
            for {
              userRepo <- UserRepository.make[IO](resource)
              _ <- IO(println("Creating users" + "_" * 50))
              johnId <- userRepo.create("John", "email@john.com")
              _ <- IO(println(s"John created with id: $johnId"))
              jacobId <- userRepo.create("Jacob", "email@jacob.com")
              _ <- IO(println(s"Jacob created with id: $jacobId"))
              kendrickId <- userRepo.create("Kendrick", "email@kendrick.com")
              _ <- IO(println(s"Kendrick created with id: $kendrickId"))
              _ <- IO(println("Fetching all users" + "_" * 50))
              users_1 <- userRepo.findAll.compile.toList
              _ <- IO(println(s"Users found: $users_1"))
              _ <- IO(println("Update John's email to: email@email.com" + "_" * 50))
              _ <- userRepo.update(User(johnId, "John", "email@email.com"))
              _ <- IO(println("Fetching all users" + "_" * 50))
              users_2 <- userRepo.findAll.compile.toList
              _ <- IO(println(s"Users found: $users_2"))
              _ <- IO(println("Deleting John" + "_" * 50))
              _ <- userRepo.delete(johnId)
              _ <- IO(println("Fetching all users" + "_" * 50))
              users_3 <- userRepo.findAll.compile.toList
              _ <- IO(println(s"Users found: $users_3"))
            } yield ExitCode.Success
}