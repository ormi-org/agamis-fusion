package io.agamis.fusion.core.db.datastore.document.exception.typed.file

case class DuplicatedFileException(
    private val message: String,
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)
