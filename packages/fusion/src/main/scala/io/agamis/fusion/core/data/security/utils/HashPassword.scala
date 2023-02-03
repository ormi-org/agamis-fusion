package io.agamis.fusion.core.data.security.utils

import at.favre.lib.crypto.bcrypt.BCrypt

object HashPassword {

    val username = "user"
        val password: String = hashPassword("admin")

    def hashPassword(password: String): String = {
        BCrypt.withDefaults().hashToString(12, password.toCharArray)
    }

    def checkPassword(userInput: String): Boolean = {
        BCrypt.verifyer().verify(userInput.toCharArray, password).verified
    }

}