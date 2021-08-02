package io.ogdt.fusion.core.db.models.sql.exceptions

final case class EntityRelationConstraintViolationException(
    private val message: String = "",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)
