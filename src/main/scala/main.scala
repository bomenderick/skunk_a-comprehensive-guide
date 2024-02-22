import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.all.*
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures
import skunk.Session
import skunk.syntax.all.*
import natchez.Trace.Implicits.noop
import com.rockthejvm.db.UserRepository
import com.rockthejvm.domain.{Config, User}
import com.rockthejvm.modules.DbConnection

object main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    val config: Either[ConfigReaderFailures, Config] = ConfigSource.default.at("db").load[Config]
    config match
      case Left(configFailure) =>
        for {
          _ <- IO(println("Failed to load configurations"))
          _ <- IO(println(configFailure.prettyPrint()))
        } yield ExitCode.Success

      case Right(configValues) =>
        DbConnection.single[IO](configValues).use {session =>
          for {
            userRepo <- UserRepository.make[IO](session)
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
}