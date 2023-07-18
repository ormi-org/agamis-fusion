package io.agamis.fusion.core.db.datastores.sql.exceptions.typed.filesystems

final case class FilesystemNotPersistedException(
    private val cause: Throwable = None.orNull,
    private val message: String = "Failed to persist fileSystem"
) extends Exception(message, cause)

object FilesystemNotPersistedException {

    def apply(
        message: String,
        cause: Throwable
    ): FilesystemNotPersistedException = {
        new FilesystemNotPersistedException(cause, message)
    }

    def apply(
        cause: Throwable
    ): FilesystemNotPersistedException = {
        new FilesystemNotPersistedException(cause)
    }

    def apply(
        message: String
    ): FilesystemNotPersistedException = {
        new FilesystemNotPersistedException(null, message)
    }
}