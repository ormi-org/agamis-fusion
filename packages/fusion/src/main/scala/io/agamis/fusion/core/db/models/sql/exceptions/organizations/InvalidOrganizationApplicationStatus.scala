package io.agamis.fusion.core.db.models.sql.exceptions.organizations

final case class InvalidOrganizationApplicationStatus(
    private val cause: Throwable = None.orNull,
    private val message: String = None.orNull
) extends Exception(message, cause)

object InvalidOrganizationApplicationStatus {

    def apply(
        message: String,
        cause: Throwable
    ): InvalidOrganizationApplicationStatus = {
        new InvalidOrganizationApplicationStatus(cause, message)
    }

    def apply(
        cause: Throwable
    ): InvalidOrganizationApplicationStatus = {
        new InvalidOrganizationApplicationStatus(cause)
    }

    def apply(
        message: String
    ): InvalidOrganizationApplicationStatus = {
        new InvalidOrganizationApplicationStatus(null, message)
    }
}