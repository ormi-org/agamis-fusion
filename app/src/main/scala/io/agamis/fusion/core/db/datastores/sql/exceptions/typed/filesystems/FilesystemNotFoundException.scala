package io.agamis.fusion.core.db.datastores.sql.exceptions.typed.filesystems

final case class FilesystemNotFoundException(
    private val message: String = "Specified fileSystem couldn't be found",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)