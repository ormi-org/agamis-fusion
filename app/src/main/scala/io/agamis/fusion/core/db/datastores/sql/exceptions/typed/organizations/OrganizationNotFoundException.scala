package io.agamis.fusion.core.db.datastores.sql.exceptions.typed.organizations

final case class OrganizationNotFoundException(
    private val message: String = "Specified organization couldn't be found",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)