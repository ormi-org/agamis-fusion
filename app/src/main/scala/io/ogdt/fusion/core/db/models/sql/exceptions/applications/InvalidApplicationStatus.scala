package io.ogdt.fusion.core.db.models.sql.exceptions.applications

final case class InvalidApplicationStatus(
    private val cause: Throwable = None.orNull,
    private val message: String = None.orNull
) extends Exception(message, cause)

object InvalidApplicationStatus {

    def apply(
        message: String,
        cause: Throwable
    ): InvalidApplicationStatus = {
        new InvalidApplicationStatus(cause, message)
    }

    def apply(
        cause: Throwable
    ): InvalidApplicationStatus = {
        new InvalidApplicationStatus(cause)
    }

    def apply(
        message: String
    ): InvalidApplicationStatus = {
        new InvalidApplicationStatus(null, message)
    }
}