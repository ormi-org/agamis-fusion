package io.ogdt.fusion.core.db.datastores.sql.exceptions.applications

final case class ApplicationNotPersistedException(
    private val cause: Throwable = None.orNull,
    private val message: String = "Failed to persist application"
) extends Exception(message, cause)

object ApplicationNotPersistedException {

    def apply(
        message: String,
        cause: Throwable
    ): ApplicationNotPersistedException = {
        new ApplicationNotPersistedException(cause, message)
    }

    def apply(
        cause: Throwable
    ): ApplicationNotPersistedException = {
        new ApplicationNotPersistedException(cause)
    }

    def apply(
        message: String
    ): ApplicationNotPersistedException = {
        new ApplicationNotPersistedException(null, message)
    }
}