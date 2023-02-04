package io.agamis.fusion.core.db.datastores.sql.exceptions.typed.permissions

final case class DuplicatePermissionException(
    private val message: String = "Specified permission has duplicates",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)