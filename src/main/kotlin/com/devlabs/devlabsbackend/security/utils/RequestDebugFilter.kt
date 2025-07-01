package com.devlabs.devlabsbackend.security.utils


import com.devlabs.devlabsbackend.common.logging.logger
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

class RequestDebugFilter : OncePerRequestFilter() {
    private val log by logger()
    
    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestURI = request.requestURI
        val method = request.method

        log.info("Request: $method $requestURI")

        val headerNames = request.headerNames
        while (headerNames.hasMoreElements()) {
            val headerName = headerNames.nextElement()
            if (headerName.lowercase() != "authorization") {
                log.debug("Header: $headerName = ${request.getHeader(headerName)}")
            } else {
                val authHeader = request.getHeader(headerName)
                log.debug("Header: $headerName = ${authHeader.substring(0, 20.coerceAtMost(authHeader.length))}...")
            }
        }

        val auth = SecurityContextHolder.getContext().authentication
        if (auth != null) {
            log.info("Authentication: ${auth.name}, Authorities: ${auth.authorities}")
        } else {
            log.info("No authentication found in security context")
        }

        try {
            filterChain.doFilter(request, response)
        } finally {
            log.info("Response status: ${response.status}")
        }
    }
}
