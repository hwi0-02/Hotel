package com.example.backend.authlogin.config;

import com.example.backend.authlogin.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    CorsConfigurationSource corsSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(List.of("https://hwiyeong.shop", "http://localhost:5173"));
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/**", c);
        return s;
    }

    // #### [ADDED] Actuator 전용 체인 (/actuator/**) ─ Prometheus/Health 공개
    @Bean
    @Order(0)
    public SecurityFilterChain actuatorChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/actuator/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/actuator/health",
                    "/actuator/info",
                    "/actuator/prometheus"
                ).permitAll()
                .anyRequest().denyAll() // 필요 시 다른 엔드포인트는 차단
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());
            //.oauth2Login(oauth2 -> oauth2.disable());
        return http.build();
    }

    // #### API 전용 보안 체인 (/api/**)
    @Bean
    @Order(1)
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")
            .cors(c -> c.configurationSource(corsSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()

                // 헬스/테스트/업로드 공개
                .requestMatchers("/api/health").permitAll()
                .requestMatchers("/api/admin/health/**").permitAll()
                .requestMatchers("/api/admin/test").permitAll()
                .requestMatchers("/api/admin/events/sse").permitAll()
                .requestMatchers("/uploads/**").permitAll()

                // OAuth2 경로 모두 허용
                .requestMatchers("/api/oauth2/**").permitAll()
                .requestMatchers("/oauth2/**").permitAll()
                .requestMatchers("/login/oauth2/**").permitAll()

                // 어드민 API
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // 회원가입/로그인 공개
                .requestMatchers(HttpMethod.POST, "/api/users/register", "/api/users/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/admins/register").permitAll()

                // 비번/테스트 공개
                .requestMatchers("/api/password/**", "/api/test/**").permitAll()

                // reCAPTCHA site key 공개 허용
                .requestMatchers("/api/config/recaptcha-site-key").permitAll()

                // ✅ 검색 공개 (/api/search/**)
                .requestMatchers("/api/search/**").permitAll()

                // ✅ 호텔 목록/상세 공개 (프론트 호환 위해 유지)
                .requestMatchers(HttpMethod.GET, "/api/hotels", "/api/hotels/**").permitAll()

                // ✅ 호텔 리뷰 통계 공개
                .requestMatchers(HttpMethod.GET, "/api/reviews/hotels/*/stats").permitAll()

                // 그 외는 인증 필요
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.authenticationEntryPoint(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
            ))
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .oauth2Login(oauth -> oauth
                .authorizationEndpoint(ep -> ep.baseUri("/api/oauth2/authorization"))
                .redirectionEndpoint(ep -> ep.baseUri("/login/oauth2/code/*"))
                .loginPage("/api/oauth2/authorization/naver")
                .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
                .successHandler(oAuth2AuthenticationSuccessHandler)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // #### 웹(정적/뷰/OAuth) 체인
    @Bean
    @Order(2)
    public SecurityFilterChain webChain(HttpSecurity http) throws Exception {
        http
            .cors(c -> c.configurationSource(corsSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/assets/**",
                    "/uploads/**",
                    "/api/health",
                    "/api/oauth2/**",
                    "/oauth2/**",
                    "/login/oauth2/**",
                    "/css/**", "/js/**", "/images/**", "/public/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .oauth2Login(oauth -> oauth
                .authorizationEndpoint(ep -> ep.baseUri("/api/oauth2/authorization"))
                .redirectionEndpoint(ep -> ep.baseUri("/login/oauth2/code/*"))
                .loginPage("/api/oauth2/authorization/naver")
                .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
                .successHandler(oAuth2AuthenticationSuccessHandler)
            );

        return http.build();
    }
}
