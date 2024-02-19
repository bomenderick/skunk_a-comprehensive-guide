package skunk_guide.domain

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

/**
  * Created by Bomen Derick.
  */
case class Config (
    host: String,
    port: Int,
    password: String,
    _user: String,
    database: String,
    maxConnections: Int
) derives ConfigReader