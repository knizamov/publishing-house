package io.github.knizamov.publishing.instrastructure.security

import io.github.knizamov.publishing.shared.security.GenericUser
import io.github.knizamov.publishing.shared.security.User
import io.github.knizamov.publishing.shared.security.UserContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

@Configuration
private open class UserContextConfiguration {

    @Bean
    protected fun userContext(): SpringSecurityUserContext {
        return SpringSecurityUserContext()
    }

    class SpringSecurityUserContext : UserContext {
        override fun authenticatedUserOrNull(): User? {
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication != null && authentication.isAuthenticated && authentication is JwtAuthenticationToken) {
                return GenericUser(
                    userId = authentication.token.subject,
                    roles = authentication.authorities.map { it.authority }
                )
            }

            return null
        }
    }
}