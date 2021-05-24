package io.ogdt.fusion.core.db.datastores.sql.exceptions.filesystems

final case class FilesystemNotFoundException(
    private val message: String = "Specified fileSystem couldn't be found",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)