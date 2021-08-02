package io.ogdt.fusion.core.db.datastores.sql.exceptions.groups

final case class GroupNotFoundException(
    private val message: String = "Specified group couldn't be found",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)