package com.rockthejvm.db

import cats.effect.Sync
import cats.syntax.all.*
import fs2.Stream
import skunk.{Codec, Command, Query, Session, Void}
import skunk.codec.all.*
import skunk.syntax.all.*
import com.rockthejvm.domain.User

import java.util.UUID

/**
  * Created by Bomen Derick.
  */
final class UserRepository[F[_]: Sync](session: Session[F])
  extends Repository[F, User](session) {
  import UserRepository.*

  def create(name: String, email: String): F[UUID] =
    for {
      cmd    <- session.prepare(insert)
      userId = UUID.randomUUID()
      _      <- cmd.execute(User(userId, name, email))
    } yield userId

  def findAll: Stream[F, User] =
    Stream.evalSeq(session.execute(selectAll))

  def findById(id: UUID): F[Option[User]] =
    findOneBy(selectById, id)

  def update(user: User): F[Unit] =
    update(_update, user)

  def delete(id: UUID): F[Unit] =
    update(_delete, id)
}

object UserRepository {
  def make[F[_]: Sync](session: Session[F]): F[UserRepository[F]] =
    Sync[F].delay(new UserRepository[F](session))

  private val codec: Codec[User] =
    (uuid, varchar, varchar).tupled.imap {
      case (id, name, email) => User(id, name, email)
    } { user => (user.id, user.name, user.email) }

  private val selectAll: Query[Void, User] =
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
       """.command.contramap(i => i)
}
