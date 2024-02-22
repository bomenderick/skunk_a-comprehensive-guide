package playground

import cats.syntax.all.*
import skunk.*
import skunk.codec.all.*
import skunk.syntax.all.*
import com.rockthejvm.domain.User


/**
  * Created by Bomen Derick.
  */
object trial {

  // Using regular tuples
  val codec: Codec[User] =
    (uuid, varchar, varchar).tupled.imap {
      case (id, name, email) => User(id, name, email)
    } { user => (user.id, user.name, user.email) }

  // Using twiddle tuples
  val codecTwiddle: Codec[User] =
    (uuid *: varchar *: varchar).imap {
      case id *: name *: email *: EmptyTuple => User(id, name, email)
    }(user => user.id *: user.name *: user.email *: EmptyTuple)

  // Using the `~` operator
  val codec2: Codec[User] =
    (uuid ~ varchar ~ varchar).imap {
      case id ~ name ~ email => User(id, name, email)
    }(user => user.id ~ user.name ~ user.email)
}
