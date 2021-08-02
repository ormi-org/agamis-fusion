package io.ogdt.fusion.core.db.datastores.sql.exceptions.permissions

final case class DuplicatePermissionException(
    private val message: String = "Specified permission has duplicates",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)