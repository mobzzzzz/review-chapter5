package sparta.nbcamp.reviewchapter5.config

import org.springframework.boot.autoconfigure.security.servlet.PathRequest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import sparta.nbcamp.reviewchapter5.infra.security.CustomAuthenticationEntrypoint
import sparta.nbcamp.reviewchapter5.infra.security.jwt.JwtAuthenticationFilter

@TestConfiguration
class TestSecurityConfig {
    @MockBean
    lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter

    @MockBean
    lateinit var customAuthenticationEntrypoint: CustomAuthenticationEntrypoint

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .csrf { it.disable() }
            .headers { header -> header.frameOptions { it.disable() } }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/sign-up",
                    "/sign-in",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/error",
                ).permitAll()
                    .requestMatchers(HttpMethod.GET, "/**").permitAll()
                    .requestMatchers(PathRequest.toH2Console()).permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling { it.authenticationEntryPoint(customAuthenticationEntrypoint) }
            .build()
    }
}