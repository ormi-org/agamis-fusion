package io.ogdt.fusion.core.db.datastores.sql.exceptions.profiles

final case class ProfileQueryExecutionException(
    private val cause: Throwable = None.orNull,
    private val message: String = "An error occured while executing profile SQL query"
) extends Exception(message, cause)

object ProfileQueryExecutionException {

    def apply(
        message: String,
        cause: Throwable
    ): ProfileQueryExecutionException = {
        new ProfileQueryExecutionException(cause, message)
    }

    def apply(
        cause: Throwable
    ): ProfileQueryExecutionException = {
        new ProfileQueryExecutionException(cause)
    }
}