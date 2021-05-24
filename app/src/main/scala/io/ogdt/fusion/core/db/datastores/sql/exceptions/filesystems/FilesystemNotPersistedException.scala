package io.ogdt.fusion.core.db.datastores.sql.exceptions.filesystems

final case class FilesystemNotPersistedException(
    private val cause: Throwable = None.orNull,
    private val message: String = "Failed to persist fileSystem"
) extends Exception(message, cause)

object FilesystemNotPersistedException {

    def apply(
        message: String,
        cause: Throwable
    ): FilesystemNotPersistedException = {
        FilesystemNotPersistedException(cause, message)
    }

    def apply(
        cause: Throwable
    ): FilesystemNotPersistedException = {
        FilesystemNotPersistedException(cause)
    }

    def apply(
        message: String
    ): FilesystemNotPersistedException = {
        FilesystemNotPersistedException(null, message)
    }
}