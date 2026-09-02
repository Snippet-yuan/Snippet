package com.snippet.security;

import com.snippet.security.handler.RestAccessDeniedHandler;
import com.snippet.security.handler.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * 密码加密器。
     * 注册时用于加密密码，登录时用于验证密码。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * HTTP 请求安全规则。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                /*
                 * 当前项目是 JSON API，不依赖浏览器自动携带 Cookie。
                 */
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api/**",
                                "/actuator/**"
                        ))

                /*
                 * 不使用 Spring Security 默认登录页和 HTTP Basic。
                 */
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                /*
                 * JWT 认证不在服务端保存登录 Session。
                 */
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/health",
                                "/api/v1/system/health",
                                "/doc.html",
                                "/webjars/**",
                                "/knife4j/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll()

                        /*
                         * 只有注册、登录和明确的公开接口不需要 JWT。
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/register",
                                "/api/v1/auth/login"
                        ).permitAll()
                        .requestMatchers("/api/v1/public/**").permitAll()

                        .anyRequest().authenticated()
                )

                /*
                 * 从 Authorization: Bearer <token> 中读取并校验 JWT。
                 */
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));

        return http.build();
    }
}
