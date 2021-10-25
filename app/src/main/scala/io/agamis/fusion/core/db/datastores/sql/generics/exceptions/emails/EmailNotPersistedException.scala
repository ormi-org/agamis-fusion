package io.agamis.fusion.core.db.datastores.sql.generics.exceptions.emails

final case class EmailNotPersistedException(
    private val cause: Throwable = None.orNull,
    private val message: String = "Failed to persist email"
) extends Exception(message, cause)

object EmailNotPersistedException {

    def apply(
        message: String,
        cause: Throwable
    ): EmailNotPersistedException = {
        new EmailNotPersistedException(cause, message)
    }

    def apply(
        cause: Throwable
    ): EmailNotPersistedException = {
        new EmailNotPersistedException(cause)
    }
}