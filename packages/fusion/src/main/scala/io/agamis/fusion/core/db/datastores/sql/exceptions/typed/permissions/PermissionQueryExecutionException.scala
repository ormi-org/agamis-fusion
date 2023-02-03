package io.agamis.fusion.core.db.datastores.sql.exceptions.typed.permissions

final case class PermissionQueryExecutionException(
    private val cause: Throwable = None.orNull,
    private val message: String = "An error occurred while executing permission SQL query"
) extends Exception(message, cause)

object PermissionQueryExecutionException {

    def apply(
        message: String,
        cause: Throwable
    ): PermissionQueryExecutionException = {
        new PermissionQueryExecutionException(cause, message)
    }

    def apply(
        cause: Throwable
    ): PermissionQueryExecutionException = {
        new PermissionQueryExecutionException(cause)
    }
}