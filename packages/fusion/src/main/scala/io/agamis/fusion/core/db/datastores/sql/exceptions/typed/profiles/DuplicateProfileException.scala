package io.agamis.fusion.core.db.datastores.sql.exceptions.typed.profiles

final case class DuplicateProfileException(
    private val message: String = "Specified profile has duplicates",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)