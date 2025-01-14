package pl.pbgym.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pl.pbgym.util.auth.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    @Autowired
    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter, AuthenticationProvider authenticationProvider) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/registerMember/**", "/auth/authenticate/**").permitAll()
                        .requestMatchers("/auth/registerTrainer/**").hasAnyAuthority("ADMIN", "TRAINER_MANAGEMENT")
                        .requestMatchers("/auth/registerWorker/**").hasAuthority("ADMIN")

                        .requestMatchers("/members/**").hasAnyAuthority("ADMIN", "MEMBER_MANAGEMENT", "MEMBER")

                        .requestMatchers("/trainers/**").hasAnyAuthority("ADMIN", "TRAINER_MANAGEMENT", "TRAINER")

                        .requestMatchers("/trainerOffers/allTrainersWithOffers").permitAll()
                        .requestMatchers("/trainerOffers/**").hasAnyAuthority("TRAINER", "TRAINER_MANAGEMENT", "ADMIN")

                        .requestMatchers("/workers/all/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/workers/{email}", "/workers/changeEmail/{email}").hasAuthority("ADMIN")
                        .requestMatchers("/workers/**", "/workers/changePassword/**").hasAnyAuthority("ADMIN", "WORKER")

                        .requestMatchers("/offers/public/active/**").permitAll()
                        .requestMatchers("/offers/**").hasAnyAuthority("ADMIN", "PASS_MANAGEMENT")

                        .requestMatchers("/passes/**").hasAnyAuthority("MEMBER", "ADMIN", "PASS_MANAGEMENT")

                        .requestMatchers("/groupClasses/upcoming/**", "/groupClasses/historical/**").permitAll()
                        .requestMatchers("/groupClasses/trainer/**", "/groupClasses/{groupClassId}/members/**").hasAnyAuthority("GROUP_CLASS_MANAGEMENT", "ADMIN", "TRAINER")
                        .requestMatchers("/groupClasses/member/**").hasAnyAuthority("GROUP_CLASS_MANAGEMENT", "ADMIN", "MEMBER")
                        .requestMatchers("/groupClasses/**").hasAnyAuthority("GROUP_CLASS_MANAGEMENT", "ADMIN")

                        .requestMatchers("/creditCardInfo/{email}/full/**").hasAuthority("MEMBER")
                        .requestMatchers( "/creditCardInfo/**").hasAnyAuthority("MEMBER", "ADMIN", "MEMBER_MANAGEMENT")

                        .requestMatchers("/gym/count/**").permitAll()
                        .requestMatchers("/gym/**").hasAuthority("WORKER")

                        .requestMatchers("/blog/all/**").permitAll()
                        .requestMatchers("/blog/**").hasAnyAuthority("BLOG", "ADMIN")

                        .requestMatchers("/trainerStatistics/**").hasAnyAuthority("STATISTICS", "ADMIN", "TRAINER_MANAGEMENT", "TRAINER")
                        .requestMatchers("/memberStatistics/**").hasAnyAuthority("STATISTICS", "ADMIN", "MEMBER_MANAGEMENT", "MEMBER")
                        .requestMatchers("/statistics/**").hasAnyAuthority("STATISTICS", "ADMIN")

                        .requestMatchers("/logs/**").hasAuthority("ADMIN")

                        .requestMatchers("/swagger/**", "/swagger-ui/**", "v3/api-docs/**").permitAll()
                        .requestMatchers("/ping/**").permitAll()
                        .anyRequest().authenticated()
                ).sessionManagement(config -> config
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ).authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}