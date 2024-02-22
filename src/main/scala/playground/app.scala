package playground


import cats.effect.*
import cats.syntax.all.*
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures
import skunk.*
import skunk.implicits.*
import skunk.codec.all.*
import natchez.Trace.Implicits.noop
import cats.effect.std.Console
import com.rockthejvm.domain.{Config, User}
import fs2.io.net.Network
import natchez.Trace

import java.util.UUID

/**
  * Created by Bomen Derick.
  */
object DConnection {
  def single[F[_] : Temporal : Trace : Network: Console](config: Config): Resource[F, Session[F]] = Session.single(
    host = config.host,
    port = config.port,
    user = config.username,
    password = Some(config.password),
    database = config.database,
  )

  def pooled[F[_] : Temporal : Trace : Network: Console](config: Config): Resource[F, Resource[F, Session[F]]] = Session.pooled(
    host = config.host,
    port = config.port,
    user = config.username,
    password = Some(config.password),
    database = config.database,
    max = 10
  )
}

object app extends IOApp {

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
  val userData: User = User(userId, "James", "jiim@email.com")

  override def run(args: List[String]): IO[ExitCode] =
    val config: Either[ConfigReaderFailures, Config] = ConfigSource.default.at("db").load[Config]
    config match
      case Left(configFailure) =>
        for {
          _ <- IO(println(configFailure.prettyPrint(2)))
        } yield ExitCode.Success

      case Right(configValues) =>
        val resource: Resource[IO, Session[IO]] = DConnection.single[IO](configValues)
//        resource.use { session =>
//          session.prepare(insert).flatMap { cmd =>
//            cmd.execute(userData).flatMap { rowCount =>
//              IO(println(s"Inserted $rowCount row(s) with id: $userId"))
//            }
//          } *> IO.pure(ExitCode.Success)
//        }

        resource.use { session =>
          for {
            command  <- session.prepare(insert)
            rowCount <- command.execute(userData)
            _        <- IO(println(s"Inserted $rowCount row(s) with id: $userId"))
          } yield ExitCode.Success
        }

//        resource.use { session =>
//          session.prepare(selectById).flatMap { pq =>
//            pq.option(UUID.fromString("67b5d283-f48a-4a00-87e0-fb60e36e9d8a")).flatMap { res =>
//              res match
//                case Some(user) => IO(println(s"User found: $user"))
//                case None       => IO(println(s"No user found with id: $userId")) *> IO.raiseError(new NoSuchElementException())
//            }
//          } *> IO.pure(ExitCode.Success)
//        }

        resource.use { session =>
          for {
            query <- session.prepare(selectById)
            res   <- query.option(UUID.fromString("67b5d283-f48a-4a00-87e0-fb60e36e9d8a"))
            _     <- res match
              case Some(user) => IO(println(s"User found: $user"))
              case None       => IO(println(s"No user found with id: $userId")) *> IO.raiseError(new NoSuchElementException())
          } yield ExitCode.Success
        }

}
