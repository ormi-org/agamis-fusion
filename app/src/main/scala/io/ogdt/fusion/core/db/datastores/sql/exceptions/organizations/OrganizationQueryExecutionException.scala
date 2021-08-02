package io.ogdt.fusion.core.db.datastores.sql.exceptions.organizations

final case class OrganizationQueryExecutionException(
    private val cause: Throwable = None.orNull,
    private val message: String = "An error occured while executing organization SQL query"
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