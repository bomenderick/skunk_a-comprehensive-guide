package skunk_guide.db

import cats.effect.{Resource, Sync}
import cats.implicits.*
import skunk_guide.errors.Errors.UniqueViolation
import skunk.{Command, Query, Session, SqlState}

/**
  * Created by Bomen Derick.
  */
trait Repository[F[_], E](resource: Resource[F, Session[F]]) {
  protected def run[A](session: Session[F] => F[A])(implicit F: Sync[F]): F[A] =
    resource.use(session).handleErrorWith {
      case SqlState.UniqueViolation(ex) =>
        UniqueViolation(ex.detail.fold(ex.message)(m => m)).raiseError[F, A]
    }

  protected def findOneBy[A](query: Query[A, E], argument: A)(implicit F: Sync[F]): F[Option[E]] =
    run { session =>
      session.prepareR(query).use { preparedQuery =>
        preparedQuery.option(argument)
      }
    }

  protected def update[A](command: Command[A], argument: A)(implicit F: Sync[F]): F[Unit] =
    run { session =>
      session.prepareR(command).use { preparedCommand =>
        preparedCommand.execute(argument).void
      }
    }

}
