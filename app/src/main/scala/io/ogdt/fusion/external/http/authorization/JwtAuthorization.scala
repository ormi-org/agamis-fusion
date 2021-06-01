package io.ogdt.fusion.external.http.authorization

import java.util.UUID
import java.util.concurrent.TimeUnit

import scala.util.Try

import pdi.jwt.{ JwtAlgorithm, JwtClaim, JwtSprayJson }

import scala.util.{Success, Failure}

import io.ogdt.fusion.external.http.entities.User
// import db user
import io.ogdt.fusion.env.EnvContainer
import scala.concurrent.Future

object JwtAuthorization {

    val privateKey = "test" // TODO à changer
    val publicKey =  "test" // TODO à changer

    val refreshTokenExpiration: Int = EnvContainer.getString("jwt.tokenExpiration.refreshTokenExpirationInSeconds").toInt
    val tokenExpiration: Int = EnvContainer.getString("jwt.tokenExpiration.tokenExpirationInSeconds").toInt

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

    // create a refresh token
    def refreshToken(/*user: User /*(db USER)*/*/): String = {
        val claims = JwtClaim(
            // Number in second
            expiration = Some(System.currentTimeMillis() / 1000 + TimeUnit.SECONDS.toSeconds(refreshTokenExpiration)),
            issuedAt = Some(System.currentTimeMillis() / 1000),
            issuer = Some("ogdt-fusion")
        ).withContent("") // mettre le user sous forme de JSON sans le mdp et avec une concaténation complète de ses permissions (user et groupe) + la liste de ses groupes
        JwtSprayJson.encode(claims,privateKey,JwtAlgorithm.RS512)
    }

    // encoding for the creation of token 
    // tokenExpiration in seconds 
    def createToken(/*user: User /*(db USER)*/*/): String = {
        val claims = JwtClaim(
            // Number in second
            expiration = Some(System.currentTimeMillis() / 1000 + TimeUnit.SECONDS.toSeconds(tokenExpiration)),
            issuedAt = Some(System.currentTimeMillis() / 1000),
            issuer = Some("ogdt-fusion")
        ).withContent("") // mettre le user sous forme de JSON sans le mdp et avec une concaténation complète de ses permissions (user et groupe) + la liste de ses groupes
        JwtSprayJson.encode(claims,privateKey,JwtAlgorithm.RS512)
    }


    def isTokenExpired(token: String): Boolean = {
        JwtSprayJson.decode(token, publicKey, Seq(JwtAlgorithm.RS512)) match {
            // if expiration is lower than the current time
            case Success(claim) => claim.expiration.getOrElse(0L) < System.currentTimeMillis() / 1000
            case Failure(_) => true
        }
    }

    def isTokenValid(token: String): Boolean = {
        JwtSprayJson.isValid(token, publicKey, Seq(JwtAlgorithm.RS512))
    }

}