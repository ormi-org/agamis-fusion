package io.agamis.fusion.core.db.datastores.sql.exceptions.typed.organizationtypes

final case class DuplicateOrganizationtypeException(
    private val message: String = "Specified organization type has duplicates",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)