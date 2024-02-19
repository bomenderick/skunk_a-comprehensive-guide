package skunk_guide.db

import cats.effect.{Resource, Sync}
import cats.implicits.*
import skunk.{Codec, Command, Query, Session, Void, ~}
import skunk.codec.all.{uuid, varchar}
import skunk.implicits.*
import skunk_guide.domain.User
import skunk_guide.db.Repository
import fs2.Stream

import java.util.UUID

/**
  * Created by Bomen Derick.
  */
private final class UserRepository[F[_]: Sync](resource: Resource[F, Session[F]]) extends Repository[F, User](resource) {
  import UserRepository.*

  def create(name: String, email: String): F[UUID] =
    run { session =>
      session.prepareR(insert).use { cmd =>
        val userId = UUID.randomUUID()
        cmd.execute(User(userId, name, email)).map(_ => userId)
      }
    }

  def findAll: Stream[F, User] =
    Stream.evalSeq(run(_.execute(selectAll)))

  def findById(id: UUID): F[Option[User]] =
    findOneBy(selectById, id)

  def update(user: User): F[Unit] =
    update(_update, user)

  def delete(id: UUID): F[Unit] =
    update(_delete, id)

}

object UserRepository {
  def make[F[_]: Sync](resource: Resource[F, Session[F]]): F[UserRepository[F]] =
    Sync[F].delay(new UserRepository[F](resource))

  private val codec: Codec[User] =
    (uuid ~ varchar ~ varchar).imap {
      case id ~ name ~ email => User(id, name, email)
    }(user => user.id ~ user.name ~ user.email)

  val selectAll: Query[Void, User] =
    sql"""
         SELECT * FROM users
       """.query(codec)

  private val selectById: Query[UUID, User] =
    sql"""
         SELECT * FROM users
         WHERE id = $uuid
       """.query(codec)

  private val insert: Command[User] =
    sql"""
         INSERT INTO users
         VALUES ($codec)
       """.command

  private val _update: Command[User] =
    sql"""
         UPDATE users
         SET name = $varchar, email = $varchar
         WHERE id = $uuid
       """.command.contramap { user => (user.name, user.email, user.id)
    }

  private val _delete: Command[UUID] =
    sql"""
         DELETE FROM users
         WHERE id = $uuid
       """.command.contramap(a => a)
}
