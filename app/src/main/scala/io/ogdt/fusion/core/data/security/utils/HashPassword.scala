package io.ogdt.fusion.core.data.security.utils

import io.ogdt.fusion.external.http.entities.User

import java.util.UUID

import at.favre.lib.crypto.bcrypt.BCrypt

object HashPassword {

    val users = Map(
        "1" -> new User(UUID.randomUUID(),"admin","admin"),
        "2" -> new User(UUID.randomUUID(),"test","test"),
        "3" -> new User(UUID.randomUUID(),"jean","michel")
    )

    def checkPassword(username: String, password: String): Boolean = {
        if(username.isEmpty() || password.isEmpty()){
            throw new Exception("Username or password is empty")   
        }else {
            if (users.exists(x => x._2.username == username)) {
                if (users.exists(x => x._2.password == password)) {
                    return true
                } else { throw new Exception("Password is invalid") }
            } else { throw new Exception("Username is invalid")    }
        }
    }

    def hashPassword(password: String): String = {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    def verifyPassword(password: String): BCrypt.Result = {
        return BCrypt.verifyer().verify(password.toCharArray(), this.hashPassword(password))
    }

}