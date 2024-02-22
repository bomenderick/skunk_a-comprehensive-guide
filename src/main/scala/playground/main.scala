package playground

import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.syntax.all.*
import com.rockthejvm.domain.{Config, User}
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures
import skunk.*
import skunk.implicits.*
import skunk.codec.all.*
import natchez.Trace.Implicits.noop
import com.rockthejvm.modules.DbConnection

import java.util.UUID

/**
  * Created by Bomen Derick.
  */
object main extends IOApp {

  // creating a single database session
  def singleSession(config: Config): IO[Unit] = DbConnection.single[IO](config).use { session =>
    for {
      _           <- IO(println("Using a single session..."))
      dateAndTime <- session.unique(sql"select current_timestamp".query(timestamptz))
      _           <- IO(println(s"Current date and time is $dateAndTime."))
    } yield ()
  }

  // creating pooled database session
  def pooledSession(config: Config): IO[Unit] = DbConnection.pooled[IO](config).use { resource =>
    resource.use { session =>
      for {
        _           <- IO(println("Using a pooled session..."))
        dateAndTime <- session.unique(sql"select current_timestamp".query(timestamptz))
        _           <- IO(println(s"Current date and time is $dateAndTime."))
      } yield ()
    }
  }

  def checkConnection(config: Config): IO[Unit] =  singleSession(config) *> pooledSession(config)

  /**
   * Defining encoders and decoders for type User
   * Using  each of this values in the sql statement below we obtain
   * the same result
  * */
  // Using regular tuples
  val codec: Codec[User] =
    (uuid, varchar, varchar).tupled.imap {
      case (id, name, email) => User(id, name, email)
    } { user => (user.id, user.name, user.email) }

  // Using twiddle tuples
  val codecTwiddle: Codec[User] =
    (uuid *: varchar *: varchar).imap {
      case id *: name *: email *: EmptyTuple => User(id, name, email)
    }(user => user.id *: user.name *: user.email *: EmptyTuple)

  // Using the `~` operator
  val codec2: Codec[User] =
    (uuid ~ varchar ~ varchar).imap {
      case id ~ name ~ email => User(id, name, email)
    }(user => user.id ~ user.name ~ user.email)

  val insert: Command[User] =
    sql"""
           INSERT INTO users
           VALUES ($codec)
         """.command

  val selectById: Query[UUID, User] =
    sql"""
           SELECT * FROM users
           WHERE id = $uuid
         """.query(codec)

  // Inserted Insert(1) row(s) with id: b68f2676-611e-4db2-af80-0458e9c52bd3
  val userId: UUID = UUID.randomUUID()
  val userData: User = User(userId, "Ramsis", "ram@email.com")

  def insertUser(session: Session[IO]): IO[UUID] =
    for {
      command  <- session.prepare(insert)
      rowCount <- command.execute(userData)
      _        <- IO(println(s"Inserted $rowCount row(s) with id: $userId"))
    } yield userId

  def searchUserById(id: UUID, session: Session[IO]): IO[Unit] =
    for {
      query <- session.prepare(selectById)
      res   <- query.option(id)
      _     <- res match
        case Some(user) => IO(println(s"User found: $user"))
        case None       => IO(println(s"No user found with id: $userId")) *> IO.raiseError(new NoSuchElementException())
    } yield ()

  override def run(args: List[String]): IO[ExitCode] =
    val config: Either[ConfigReaderFailures, Config] = ConfigSource.default.at("db").load[Config]
    config match
      case Left(configFailure) =>
        for {
          _ <- IO(println(configFailure.prettyPrint(2)))
        } yield ExitCode.Success

      case Right(configValues) =>
        IO.println("Testing single and pooled sessions......") *>
        checkConnection(configValues) *>
          IO.println("Sessions established... Continuing with Single Database Session") *>
          DbConnection.single[IO](configValues).use { session =>
          for {
            id <- insertUser(session)
            _  <- searchUserById(id, session)
          }  yield ()
        } *> IO.pure(ExitCode.Success)

}
