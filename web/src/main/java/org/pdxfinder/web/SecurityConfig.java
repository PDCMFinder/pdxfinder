package org.pdxfinder.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .headers()
                .xssProtection();

    }


    @Component
    public class PdxFinderURLFilter implements Filter {

        @Override
        public void destroy() {}


        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterchain)
                throws IOException, ServletException {

            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;

            System.out.println("Query String:"+request.getQueryString());
            System.out.println("Remote Address:"+request.getRequestURL().toString());


            System.out.println("Remote Address SANITIZED:"+sanitize(request.getQueryString()));


            if (request.getRequestURI().contains("JAX")){
                response.sendRedirect("http://localhost:8080/data/search");
            }else {
                filterchain.doFilter(servletRequest, servletResponse);
            }

        }

        @Override
        public void init(FilterConfig filterconfig) throws ServletException {

        }

        public String sanitize(String string) {

            return string
                    .replaceAll("(?i)<script.*?>.*?</script.*?>", "")   // script tags
                    .replaceAll("(?i)<.*?.*?>.*?</.*?>", "") // js calls
                    .replaceAll("(?i)<.*?\\s+on.*?>.*?</.*?>", "");     // remove "font-size:11.0pt" lang="EN-U

        }
    }


}
