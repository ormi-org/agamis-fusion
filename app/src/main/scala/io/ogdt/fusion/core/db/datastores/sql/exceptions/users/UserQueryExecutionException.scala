package io.ogdt.fusion.core.db.datastores.sql.exceptions.users

final case class UserQueryExecutionException(
    private val cause: Throwable = None.orNull,
    private val message: String = "An error occured while executing user SQL query"
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