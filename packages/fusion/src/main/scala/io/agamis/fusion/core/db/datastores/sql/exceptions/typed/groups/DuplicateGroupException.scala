package io.agamis.fusion.core.db.datastores.sql.exceptions.typed.groups

final case class DuplicateGroupException(
    private val message: String = "Specified group has duplicates",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)