package io.ogdt.fusion.external.http.authorization

import java.util.concurrent.TimeUnit
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtSprayJson}
import scala.util.{Success, Failure}


trait JwtAuthorization {

    val algorithm = JwtAlgorithm.HS256
    val secretKey = "secretKey" // TODO changes, security problem

    def checkPassword(username: String, password: String): Boolean = ???

    // tokenExpiration in day 
    def createToken(username: String, tokenExpiration: Int): String = {
        val claims = JwtClaim(
            // Number in second
            expiration = Some(System.currentTimeMillis() / 1000 + TimeUnit.DAYS.toSeconds(tokenExpiration)),
            issuedAt = Some(System.currentTimeMillis() / 1000),
            issuer = Some("ogdt-fusion")
        )
        JwtSprayJson.encode(claims,secretKey,algorithm)
    }

    def isTokenExpired(token: String): Boolean = {
        JwtSprayJson.decode(token, secretKey, Seq(algorithm)) match {
            // Si l'expiration est inférieur à l'heure actuelle 
            case Success(claims) => claims.expiration.getOrElse(0L) < System.currentTimeMillis() / 1000
            case Failure(_) => true
        }
    }

    def isTokenValid(token: String): Boolean = {
        JwtSprayJson.isValid(token, secretKey, Seq(algorithm))
    }

}