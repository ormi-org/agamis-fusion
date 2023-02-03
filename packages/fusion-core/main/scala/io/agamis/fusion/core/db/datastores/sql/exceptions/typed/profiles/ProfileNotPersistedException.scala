package io.agamis.fusion.core.db.datastores.sql.exceptions.typed.profiles

final case class ProfileNotPersistedException(
    private val cause: Throwable = None.orNull,
    private val message: String = "Failed to persist profile"
) extends Exception(message, cause)

object ProfileNotPersistedException {

    def apply(
        message: String,
        cause: Throwable
    ): ProfileNotPersistedException = {
        new ProfileNotPersistedException(cause, message)
    }

    def apply(
        cause: Throwable
    ): ProfileNotPersistedException = {
        new ProfileNotPersistedException(cause)
    }

    def apply(
        message: String
    ): ProfileNotPersistedException = {
        new ProfileNotPersistedException(null, message)
    }
}