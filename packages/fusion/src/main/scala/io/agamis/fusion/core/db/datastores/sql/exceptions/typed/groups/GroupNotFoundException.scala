package io.agamis.fusion.core.db.datastores.sql.exceptions.typed.groups

final case class GroupNotFoundException(
    private val message: String = "Specified group couldn't be found",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)