package com.rockthejvm.domain

import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.generic.derivation.default.*

final case class Config (
    host: String,
    port: Int,
    username: String,
    password: String,
    database: String
) derives ConfigReader
