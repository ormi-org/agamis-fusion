package io.agamis.fusion.core.db.datastores.sql.exceptions.typed.users

final case class UserQueryExecutionException(
    private val cause: Throwable = None.orNull,
    private val message: String = "An error occurred while executing user SQL query"
) extends Exception(message, cause)

object UserQueryExecutionException {

    def apply(
        message: String,
        cause: Throwable
    ): UserQueryExecutionException = {
        new UserQueryExecutionException(cause, message)
    }

    def apply(
        cause: Throwable
    ): UserQueryExecutionException = {
        new UserQueryExecutionException(cause)
    }
}