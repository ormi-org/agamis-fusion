package io.ogdt.fusion.core.db.datastores.sql.exceptions.users

final case class UserNotPersistedException(
    private val cause: Throwable = None.orNull,
    private val message: String = "Failed to persist user"
) extends Exception(message, cause)

object UserNotPersistedException {

    def apply(
        message: String,
        cause: Throwable
    ): UserNotPersistedException = {
        UserNotPersistedException(cause, message)
    }

    def apply(
        cause: Throwable
    ): UserNotPersistedException = {
        UserNotPersistedException(cause)
    }

    def apply(
        message: String
    ): UserNotPersistedException = {
        UserNotPersistedException(null, message)
    }
}