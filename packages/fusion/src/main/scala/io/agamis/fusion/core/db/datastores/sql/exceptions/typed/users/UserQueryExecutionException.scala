package io.agamis.fusion.core.db.datastores.sql.exceptions.typed.users

final case class UserQueryExecutionException(
    private val message: String = "An error occurred while executing user SQL query",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)

object UserQueryExecutionException {

    def apply(
        message: String,
        cause: Throwable
    ): UserQueryExecutionException = {
        new UserQueryExecutionException(message, cause)
    }

    def apply(
        cause: Throwable
    ): UserQueryExecutionException = {
        new UserQueryExecutionException("An error occured while executing user SQL query", cause)
    }
}