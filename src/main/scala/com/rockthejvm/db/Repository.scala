package com.rockthejvm.db

import cats.effect.{Resource, Sync}
import cats.syntax.all.*
import com.rockthejvm.errors.Errors.UniqueViolation
import skunk.{Command, Query, Session, SqlState}

trait Repository[F[_], E](session: Session[F]) {
  protected def findOneBy[A](query: Query[A, E], argument: A)(using F: Sync[F]): F[Option[E]] =
    for {
      preparedQuery <- session.prepare(query)
      result <- preparedQuery.option(argument)
    } yield result

  protected def update[A](command: Command[A], argument: A)(using F: Sync[F]): F[Unit] =
    for {
      preparedCommand <- session.prepare(command)
      _ <- preparedCommand.execute(argument)
    } yield ()
}
