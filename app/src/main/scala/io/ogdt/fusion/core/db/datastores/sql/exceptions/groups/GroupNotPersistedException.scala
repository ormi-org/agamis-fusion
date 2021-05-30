package io.ogdt.fusion.core.db.datastores.sql.exceptions.groups

final case class GroupNotPersistedException(
    private val cause: Throwable = None.orNull,
    private val message: String = "Failed to persist group"
) extends Exception(message, cause)

object GroupNotPersistedException {

    def apply(
        message: String,
        cause: Throwable
    ): GroupNotPersistedException = {
        GroupNotPersistedException(cause, message)
    }

    def apply(
        cause: Throwable
    ): GroupNotPersistedException = {
        GroupNotPersistedException(cause)
    }

    def apply(
        message: String
    ): GroupNotPersistedException = {
        GroupNotPersistedException(null, message)
    }
}