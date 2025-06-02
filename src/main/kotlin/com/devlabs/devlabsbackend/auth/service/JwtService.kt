package com.devlabs.devlabsbackend.auth.service

import com.devlabs.devlabsbackend.user.domain.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService {

    @Value("\${jwt.secret:mySecretKey}")
    private lateinit var secret: String

    @Value("\${jwt.expiration:86400000}") // 24 hours in milliseconds
    private var jwtExpiration: Long = 86400000

    private fun getSigningKey(): SecretKey {
        val keyBytes = secret.toByteArray()
        return Keys.hmacShaKeyFor(keyBytes)
    }

    fun extractUsername(token: String): String {
        return extractClaim(token, Claims::getSubject)
    }

    fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }

    fun generateToken(user: User): String {
        return generateToken(mapOf(), user)
    }

    fun generateToken(extraClaims: Map<String, Any>, user: User): String {
        return buildToken(extraClaims, user, jwtExpiration)
    }

    private fun buildToken(
        extraClaims: Map<String, Any>,
        user: User,
        expiration: Long
    ): String {
        val claims = mutableMapOf<String, Any>()
        claims.putAll(extraClaims)
        claims["userId"] = user.id.toString()
        claims["email"] = user.email
        claims["role"] = user.role.name
        claims["profileId"] = user.profileId.toString()
        claims["name"] = user.name

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(user.email)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return (username == userDetails.username) && !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean {
        return extractExpiration(token).before(Date())
    }

    private fun extractExpiration(token: String): Date {
        return extractClaim(token, Claims::getExpiration)
    }    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .body
    }
}
