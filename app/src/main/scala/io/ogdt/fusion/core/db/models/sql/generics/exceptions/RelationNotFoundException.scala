package io.ogdt.fusion.core.db.models.sql.generics.exceptions

final case class RelationNotFoundException(
    private val message: String = "This relation doesn't exist",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)
