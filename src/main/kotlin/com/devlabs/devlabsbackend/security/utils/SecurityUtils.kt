package com.devlabs.devlabsbackend.security.utils



import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt

object SecurityUtils {

        fun getCurrentUserId(): String? {
            val authentication = SecurityContextHolder.getContext().authentication

            return if (authentication != null && authentication.isAuthenticated) {
                if (authentication.principal is Jwt) {
                    (authentication.principal as Jwt).subject
                } else {
                    authentication.name
                }
            } else {
                null
            }
        }

        fun getCurrentJwt(): Jwt? {
            val authentication = SecurityContextHolder.getContext().authentication

            return if (authentication != null && authentication.isAuthenticated) {
                authentication.principal as? Jwt
            } else {
                null
            }
        }

        fun getCurrentJwtClaim(claimName: String): String? {
            val jwt = getCurrentJwt()
            return jwt?.getClaimAsString(claimName)
        }
    }
