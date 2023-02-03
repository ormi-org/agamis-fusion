package io.agamis.fusion.core.db.datastores.sql.exceptions.typed.organizations

final case class OrganizationQueryExecutionException(
    private val cause: Throwable = None.orNull,
    private val message: String = "An error occurred while executing organization SQL query"
) extends Exception(message, cause)

object OrganizationQueryExecutionException {

    def apply(
        message: String,
        cause: Throwable
    ): OrganizationQueryExecutionException = {
        new OrganizationQueryExecutionException(cause, message)
    }

    def apply(
        cause: Throwable
    ): OrganizationQueryExecutionException = {
        new OrganizationQueryExecutionException(cause)
    }
}