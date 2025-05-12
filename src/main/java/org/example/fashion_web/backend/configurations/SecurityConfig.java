package org.example.fashion_web.backend.configurations;

import org.example.fashion_web.backend.services.servicesimpl.CustomSuccessHandler;
import org.example.fashion_web.backend.services.servicesimpl.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Autowired
    CustomSuccessHandler customSuccessHandler;

    @Autowired
    CustomUserDetailsService customUserDetailsService;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        http.csrf(c -> c.disable())

                .authorizeHttpRequests(request -> request
                                .requestMatchers("/admin/**").hasAuthority("ADMIN")
                                .requestMatchers("/products").hasAuthority("ADMIN")  // Chỉ Admin được truy cập
                                .requestMatchers("/add").hasAuthority("ADMIN")
                                .requestMatchers("/edit").hasAuthority("ADMIN")
                                .requestMatchers("/user-page").hasAuthority("USER")
                                .requestMatchers("/", "/admin/chat", "/ws", "/registration", "/login",
                                        "/user", "/products", "/refresh-token", "/css/**", "/js/**", "/pics/**",
                                        "/pics/logo/**","/fontawesome-6.7.1/**", "/about",
                                        "/user/shop/search").permitAll()
                        .anyRequest().authenticated())

                .formLogin(form -> form.loginPage("/login").loginProcessingUrl("/login")
                        .successHandler(customSuccessHandler).permitAll())

                .logout(form -> form.invalidateHttpSession(true).clearAuthentication(true)
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout").permitAll());

        return http.build();

    }


    @Autowired
    public void configure (AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());

    }

}