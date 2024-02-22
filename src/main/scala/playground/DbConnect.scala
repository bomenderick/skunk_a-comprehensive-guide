package playground

import cats.effect.{Resource, Temporal}
import cats.effect.std.Console
import com.rockthejvm.domain.Config
import fs2.io.net.Network
import natchez.Trace
import skunk.Session

/**
  * Created by Bomen Derick.
  */
object DbConnect {
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
