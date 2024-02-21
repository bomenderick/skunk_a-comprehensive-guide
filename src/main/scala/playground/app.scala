package playground


import cats.effect.*
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures
import skunk_guide.domain.{Config, User}
import skunk.*
import skunk.implicits.*
import skunk.codec.all.*
import natchez.Trace.Implicits.noop
import cats.effect.std.Console
import fs2.io.net.Network
import natchez.Trace

import java.util.UUID

/**
  * Created by Bomen Derick.
  */
final class DConnection[F[_] : Temporal : Trace : Network: Console] {
  def single(config: Config): Resource[F, Session[F]] = Session.single(
    host = config.host,
    port = config.port,
    user = config.username,
    password = Some(config.password),
    database = config.database,
  )

  def pooled(config: Config): Resource[F, Resource[F, Session[F]]] = Session.pooled(
    host = config.host,
    port = config.port,
    user = config.username,
    password = Some(config.password),
    database = config.database,
    max = 10
  )
}

object app extends IOApp {

  val codec: Codec[User] =
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
  val userData: User = User(userId, "Jacob", "jacob@email.com")

  override def run(args: List[String]): IO[ExitCode] =
    val config: Either[ConfigReaderFailures, Config] = ConfigSource.default.load[Config]
    config match
      case Left(configFailure) =>
        for {
          _ <- IO(println(configFailure.prettyPrint(2)))
        } yield ExitCode.Success

      case Right(configValues) =>
        val resource: Resource[IO, Session[IO]] = new DConnection[IO].single(configValues)
//        resource.use { session =>
//          session.prepare(insert).flatMap { cmd =>
//            cmd.execute(userData).flatMap { rowCount =>
//              IO(println(s"Inserted $rowCount row(s) with id: $userId"))
//            }
//          } *> IO.pure(ExitCode.Success)
//        }

        resource.use { session =>
          session.prepare(selectById).flatMap { pq =>
            pq.option(UUID.fromString("b68f2676-611e-4db2-af80-0458e9c52bd3")).flatMap { res =>
              res match
                case Some(user) => IO(println(s"User found: $user"))
                case None       => IO(println(s"No user found with id: $userId")) *> IO.raiseError(new NoSuchElementException())
            }
          } *> IO.pure(ExitCode.Success)
        }

}
