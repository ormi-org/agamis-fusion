package io.ogdt.fusion.core.db.datastores.sql.exceptions.filesystems

final case class FilesystemQueryExecutionException(
    private val cause: Throwable = None.orNull,
    private val message: String = "An error occured while executing filesystem SQL query"
) extends Exception(message, cause)

object FilesystemQueryExecutionException {

    def apply(
        message: String,
        cause: Throwable
    ): FilesystemQueryExecutionException = {
        new FilesystemQueryExecutionException(cause, message)
    }

    def apply(
        cause: Throwable
    ): FilesystemQueryExecutionException = {
        new FilesystemQueryExecutionException(cause)
    }
}