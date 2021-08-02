package io.ogdt.fusion.core.db.datastores.sql.exceptions.filesystems

final case class DuplicateFilesystemException(
    private val message: String = "Specified fileSystem has duplicates",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)