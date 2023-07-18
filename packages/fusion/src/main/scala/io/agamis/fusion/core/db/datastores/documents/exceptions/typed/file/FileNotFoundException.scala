package io.agamis.fusion.core.db.datastores.documents.exceptions.typed.file

final case class FileNotFoundException(
                                        private val message: String = "Specified file couldn't not be found",
                                        private val cause: Throwable = None.orNull
                                      ) extends Exception(message, cause)
