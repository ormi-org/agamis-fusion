package io.agamis.fusion.core.db.datastores.sql.exceptions.typed.organizationtypes

final case class OrganizationtypeNotFoundException(
    private val message: String = "Specified organization type couldn't be found",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)