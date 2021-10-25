package io.agamis.fusion.core.db.datastores.sql.exceptions.typed.groups

final case class GroupNotPersistedException(
    private val cause: Throwable = None.orNull,
    private val message: String = "Failed to persist group"
) extends Exception(message, cause)

object GroupNotPersistedException {

    def apply(
        message: String,
        cause: Throwable
    ): GroupNotPersistedException = {
        new GroupNotPersistedException(cause, message)
    }

    def apply(
        cause: Throwable
    ): GroupNotPersistedException = {
        new GroupNotPersistedException(cause)
    }

    def apply(
        message: String
    ): GroupNotPersistedException = {
        new GroupNotPersistedException(null, message)
    }
}