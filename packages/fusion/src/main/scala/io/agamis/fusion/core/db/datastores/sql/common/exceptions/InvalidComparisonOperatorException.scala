package io.agamis.fusion.core.db.datastores.sql.common.exceptions

final case class InvalidComparisonOperatorException(
    private val message: String = "Provided comparison operator is invalid",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)

object InvalidComparisonOperatorException {
  def apply(operator: String): InvalidComparisonOperatorException = {
    InvalidComparisonOperatorException(s"Provided comparison operator '${operator}' is invalid")
  }
}
