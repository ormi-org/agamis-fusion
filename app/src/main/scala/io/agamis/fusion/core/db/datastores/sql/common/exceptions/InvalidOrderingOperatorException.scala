package io.agamis.fusion.core.db.datastores.sql.common.exceptions

final case class InvalidOrderingOperatorException(
    private val message: String = "Provided ordering operator is invalid",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)

object InvalidOrderingOperatorException {
  def apply(operator: Int): InvalidOrderingOperatorException = {
    InvalidOrderingOperatorException(s"Provided ordering operator '${operator}' is invalid")
  }
}
