package io.ogdt.fusion.core.db.datastores.sql.exceptions.organizations

final case class OrganizationNotPersistedException(
    private val cause: Throwable = None.orNull,
    private val message: String = "Failed to persist organization"
) extends Exception(message, cause)

object OrganizationNotPersistedException {

    def apply(
        message: String,
        cause: Throwable
    ): OrganizationNotPersistedException = {
        OrganizationNotPersistedException(cause, message)
    }

    def apply(
        cause: Throwable
    ): OrganizationNotPersistedException = {
        OrganizationNotPersistedException(cause)
    }

    def apply(
        message: String
    ): OrganizationNotPersistedException = {
        OrganizationNotPersistedException(null, message)
    }
}