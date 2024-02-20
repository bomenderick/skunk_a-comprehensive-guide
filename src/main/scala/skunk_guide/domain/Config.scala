package skunk_guide.domain

import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.generic.derivation.default.*

/**
  * Created by Bomen Derick.
  */
final case class Config (
    host: String,
    port: Int,
    username: String,
    password: String,
    database: String
) derives ConfigReader
