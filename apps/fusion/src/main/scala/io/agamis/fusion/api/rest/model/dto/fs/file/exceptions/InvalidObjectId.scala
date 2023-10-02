package io.agamis.fusion.api.rest.model.dto.fs.file.exceptions

final case class InvalidObjectId(
    private val message: String = "",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)
