package io.ogdt.fusion.core.db.datastores.sql.exceptions.applications

final case class ApplicationQueryExecutionException(
    private val cause: Throwable = None.orNull,
    private val message: String = "An error occured while executing application SQL query"
) extends Exception(message, cause)

object ApplicationQueryExecutionException {

    def apply(
        message: String,
        cause: Throwable
    ): ApplicationQueryExecutionException = {
        ApplicationQueryExecutionException(cause, message)
    }

    def apply(
        cause: Throwable
    ): ApplicationQueryExecutionException = {
        ApplicationQueryExecutionException(cause)
    }
}