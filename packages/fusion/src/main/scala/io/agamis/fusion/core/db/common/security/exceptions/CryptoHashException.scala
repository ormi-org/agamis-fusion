package io.agamis.fusion.core.db.common.security.exceptions

final case class CryptoHashException(
    private val message: String = "",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)
