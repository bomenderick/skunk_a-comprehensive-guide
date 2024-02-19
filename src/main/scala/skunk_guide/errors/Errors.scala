package skunk_guide.errors

/**
  * Created by Bomen Derick.
  */
object Errors {
  sealed trait AppError extends Throwable {
    def message: String
    override def getMessage: String = message
  }

  sealed trait BadRequestError extends AppError

  final case class UniqueViolation(message: String) extends BadRequestError
}
