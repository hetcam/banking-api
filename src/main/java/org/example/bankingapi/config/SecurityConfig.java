package org.example.bankingapi.config;

import org.example.bankingapi.security.CustomUserDetailsService;
import org.example.bankingapi.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/accounts", "/api/accounts/**").hasAuthority("ACCOUNTS_READ")
                .antMatchers(HttpMethod.POST, "/api/accounts").hasAuthority("ACCOUNTS_WRITE")
                .antMatchers(HttpMethod.PUT, "/api/accounts/**").hasAuthority("ACCOUNTS_WRITE")
                .antMatchers(HttpMethod.DELETE, "/api/accounts/**").hasAuthority("ACCOUNTS_WRITE")
                .antMatchers(HttpMethod.GET, "/api/users", "/api/users/**").hasAuthority("USERS_READ")
                .antMatchers(HttpMethod.POST, "/api/users").hasAuthority("USERS_WRITE")
                .antMatchers(HttpMethod.PUT, "/api/users/**").hasAuthority("USERS_WRITE")
                .antMatchers(HttpMethod.DELETE, "/api/users/**").hasAuthority("USERS_WRITE")
                .antMatchers(HttpMethod.GET, "/api/roles", "/api/roles/**").hasAuthority("ROLES_READ")
                .antMatchers(HttpMethod.POST, "/api/roles").hasAuthority("ROLES_WRITE")
                .antMatchers(HttpMethod.PUT, "/api/roles/**").hasAuthority("ROLES_WRITE")
                .antMatchers(HttpMethod.DELETE, "/api/roles/**").hasAuthority("ROLES_WRITE")
                .anyRequest().authenticated()
                .and()
                .headers().frameOptions().sameOrigin()
                .and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
