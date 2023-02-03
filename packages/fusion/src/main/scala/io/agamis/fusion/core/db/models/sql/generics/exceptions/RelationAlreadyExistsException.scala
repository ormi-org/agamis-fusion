package io.agamis.fusion.core.db.models.sql.generics.exceptions

final case class RelationAlreadyExistsException(
    private val message: String = "This relation is already set",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)
