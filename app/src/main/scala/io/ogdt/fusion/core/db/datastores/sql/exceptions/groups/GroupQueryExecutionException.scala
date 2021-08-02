package io.ogdt.fusion.core.db.datastores.sql.exceptions.groups

final case class GroupQueryExecutionException(
    private val cause: Throwable = None.orNull,
    private val message: String = "An error occured while executing group SQL query"
) extends Exception(message, cause)

object GroupQueryExecutionException {

    def apply(
        message: String,
        cause: Throwable
    ): GroupQueryExecutionException = {
        new GroupQueryExecutionException(cause, message)
    }

    def apply(
        cause: Throwable
    ): GroupQueryExecutionException = {
        new GroupQueryExecutionException(cause)
    }
}