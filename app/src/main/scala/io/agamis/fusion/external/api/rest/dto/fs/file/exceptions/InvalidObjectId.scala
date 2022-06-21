package io.agamis.fusion.external.api.rest.dto.fs.file.exceptions

final case class InvalidObjectId(
    private val message: String = "",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)
