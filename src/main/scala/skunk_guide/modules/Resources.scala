package skunk_guide.modules

import cats.effect.{Resource, Temporal}
import cats.effect.std.Console
import fs2.io.net.Network
import natchez.Trace
import skunk.Session
import skunk_guide.domain.Config

/**
  * Created by Bomen Derick.
  */
final class Resources[F[_] : Temporal : Trace : Network: Console] {
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

