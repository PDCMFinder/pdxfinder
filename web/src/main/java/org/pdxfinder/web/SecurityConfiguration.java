package org.pdxfinder.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final static Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/")
                .permitAll();

        http
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/**").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/**").authenticated()
                .antMatchers(HttpMethod.DELETE, "/api/**").authenticated()
                .antMatchers(HttpMethod.PATCH, "/api/**").authenticated()
        ;

        http.csrf().requireCsrfProtectionMatcher(
                new AndRequestMatcher(
                        // Apply CSRF protection to all paths that match the ones below

                        new AntPathRequestMatcher("/api*/**", HttpMethod.GET.toString()),
                        new AntPathRequestMatcher("/api*/**", HttpMethod.HEAD.toString()),
                        new AntPathRequestMatcher("/api*/**", HttpMethod.OPTIONS.toString()),
                        new AntPathRequestMatcher("/api*/**", HttpMethod.TRACE.toString())
                )
        );
        http.addFilterAfter(new CsrfTokenResponseCookieBindingFilter(), CsrfFilter.class); // CSRF tokens handling

    }

    public class CSRF {
        /**
         * The name of the cookie with the CSRF token sent by the server as a response.
         */
        public static final String RESPONSE_COOKIE_NAME = "CSRF-TOKEN";
        /**
         * The name of the header carrying the CSRF token, expected in CSRF-protected requests to the server.
         */
        public static final String REQUEST_HEADER_NAME = "X-CSRF-TOKEN";
    }

    public class CsrfTokenResponseCookieBindingFilter extends OncePerRequestFilter {

        protected static final String REQUEST_ATTRIBUTE_NAME = "_csrf";

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {

            CsrfToken token = (CsrfToken) request.getAttribute(REQUEST_ATTRIBUTE_NAME);

            Cookie cookie = new Cookie(SecurityConfiguration.CSRF.RESPONSE_COOKIE_NAME, token.getToken());
            cookie.setPath("/");

            response.addCookie(cookie);

            filterChain.doFilter(request, response);
        }
    }

}