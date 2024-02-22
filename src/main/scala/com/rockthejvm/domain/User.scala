package com.rockthejvm.domain

import java.util.UUID

/**
  * Created by Bomen Derick.
  */
final case class User (
    id: UUID,
    name: String,
    email: String
)
