package io.agamis.fusion.core.db.datastores.sql.exceptions.typed.users

final case class UserNotPersistedException(
    private val message: String = "Failed to persist user",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)

object UserNotPersistedException {

    def apply(
        message: String,
        cause: Throwable
    ): UserNotPersistedException = {
        new UserNotPersistedException(message, cause)
    }

    def apply(
        cause: Throwable
    ): UserNotPersistedException = {
        new UserNotPersistedException(null, cause)
    }

    def apply(
        message: String
    ): UserNotPersistedException = {
        new UserNotPersistedException(message)
    }
}