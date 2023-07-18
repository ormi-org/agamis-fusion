package io.agamis.fusion.core.db.datastores.sql.exceptions.typed.filesystems

final case class DuplicateFilesystemException(
    private val message: String = "Specified fileSystem has duplicates",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)