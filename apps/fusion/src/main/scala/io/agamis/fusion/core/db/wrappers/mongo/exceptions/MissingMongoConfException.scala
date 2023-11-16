package io.agamis.fusion.core.db.wrappers.mongo.exceptions

final case class MissingMongoConfException(
    private val message: String = "", 
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)