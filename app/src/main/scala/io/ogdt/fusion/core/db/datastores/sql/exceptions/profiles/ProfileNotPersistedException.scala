package io.ogdt.fusion.core.db.datastores.sql.exceptions.profiles

final case class ProfileNotPersistedException(
    private val cause: Throwable = None.orNull,
    private val message: String = "Failed to persist profile"
) extends Exception(message, cause)

object ProfileNotPersistedException {

    def apply(
        message: String,
        cause: Throwable
    ): ProfileNotPersistedException = {
        ProfileNotPersistedException(cause, message)
    }

    def apply(
        cause: Throwable
    ): ProfileNotPersistedException = {
        ProfileNotPersistedException(cause)
    }

    def apply(
        message: String
    ): ProfileNotPersistedException = {
        ProfileNotPersistedException(null, message)
    }
}