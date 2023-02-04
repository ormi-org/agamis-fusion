package io.agamis.fusion.core.db.datastores.documents.exceptions.typed.file

case class DuplicatedFileException(
                                    private val message: String,
                                    private val cause: Throwable = None.orNull
                                  ) extends Exception(message, cause)
