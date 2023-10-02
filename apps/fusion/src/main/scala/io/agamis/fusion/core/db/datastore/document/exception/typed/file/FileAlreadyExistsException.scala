package io.agamis.fusion.core.db.datastore.document.exception.typed.file

final case class FileAlreadyExistsException(
    private val message: String = "Specified file already exist",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)
