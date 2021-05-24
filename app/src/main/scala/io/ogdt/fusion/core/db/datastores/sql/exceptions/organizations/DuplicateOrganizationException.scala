package io.ogdt.fusion.core.db.datastores.sql.exceptions.organizations

final case class DuplicateOrganizationException(
    private val message: String = "Specified organization has duplicates",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)