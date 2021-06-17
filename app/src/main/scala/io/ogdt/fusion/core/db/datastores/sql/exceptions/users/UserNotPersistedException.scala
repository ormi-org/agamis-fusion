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
        new UserNotPersistedException(cause, message)
    }

    def apply(
        cause: Throwable
    ): UserNotPersistedException = {
        new UserNotPersistedException(cause)
    }

    def apply(
        message: String
    ): UserNotPersistedException = {
        new UserNotPersistedException(null, message)
    }
}