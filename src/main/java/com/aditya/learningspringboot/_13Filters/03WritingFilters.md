# Spring Boot Filters – Complete Notes with Code
------------------------------------------------
1. What is a Filter in Spring Boot?
------------------------------------------------
- A Filter is part of the Servlet specification (javax.servlet.Filter / jakarta.servlet.Filter).
- It runs BEFORE the request reaches Spring's DispatcherServlet.
- It can also run AFTER the response leaves DispatcherServlet.
- Filters are container-level components, not Spring MVC components.
- Typical use cases:
    - Logging
    - Authentication / Authorization
    - Request/Response modification
    - CORS
    - Security (Spring Security internally uses filters)

Flow:
Client
-> Filter(s)
-> DispatcherServlet
-> Interceptor
-> Controller
-> Interceptor (postHandle / afterCompletion)
-> Filter(s)
-> Client

------------------------------------------------
2. How to Write a Filter (Implementing Filter Interface)
------------------------------------------------
Steps:
1. Create a class
2. Implement Filter interface
3. Override doFilter()
4. Register the filter using FilterRegistrationBean

------------------------------------------------
3. Filter #1 – LoggingFilter
------------------------------------------------
Purpose:
- Logs request URI
- Logs request processing start and end

Code:
------------------------------------------------
package com.example.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

public class LoggingFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        System.out.println("[LoggingFilter] Request URI: " + httpRequest.getRequestURI());

        // Pass request to next filter or DispatcherServlet
        chain.doFilter(request, response);

        System.out.println("[LoggingFilter] Response completed for URI: " + httpRequest.getRequestURI());
    }
}

------------------------------------------------
4. Filter #2 – AuthenticationFilter
------------------------------------------------
Purpose:
- Performs simple header-based authentication check
- Blocks request if header is missing

Code:
------------------------------------------------
package com.example.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String authHeader = httpRequest.getHeader("X-AUTH-TOKEN");

        if (authHeader == null || authHeader.isEmpty()) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("Unauthorized: Missing X-AUTH-TOKEN");
            return; // STOP request flow
        }

        // Continue request if authenticated
        chain.doFilter(request, response);
    }
}

------------------------------------------------
5. Registering Multiple Filters with URL Mapping and Order
------------------------------------------------
Important Points:
- Filters must be registered explicitly using FilterRegistrationBean
- Order determines execution sequence (LOWER value = HIGHER priority)
- URL patterns decide which requests the filter applies to

------------------------------------------------
6. Filter Configuration Class
------------------------------------------------
Code:
------------------------------------------------
package com.example.config;

import com.example.filters.LoggingFilter;
import com.example.filters.AuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<LoggingFilter> loggingFilter() {
        FilterRegistrationBean<LoggingFilter> registrationBean =
                new FilterRegistrationBean<>();

        registrationBean.setFilter(new LoggingFilter());
        registrationBean.addUrlPatterns("/*");          // Apply to all URLs
        registrationBean.setOrder(1);                   // Executes FIRST

        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<AuthenticationFilter> authenticationFilter() {
        FilterRegistrationBean<AuthenticationFilter> registrationBean =
                new FilterRegistrationBean<>();

        registrationBean.setFilter(new AuthenticationFilter());
        registrationBean.addUrlPatterns("/api/*");      // Apply only to /api/*
        registrationBean.setOrder(2);                   // Executes AFTER LoggingFilter

        return registrationBean;
    }
}

------------------------------------------------
7. Execution Order (Very Important for Interviews)
------------------------------------------------
Request to /api/users

Order:
1. LoggingFilter (order = 1)
2. AuthenticationFilter (order = 2)
3. DispatcherServlet
4. Controller
5. AuthenticationFilter (response phase)
6. LoggingFilter (response phase)

------------------------------------------------
8. Key Interview Rules (Hand Rule)
------------------------------------------------
Use FILTER when:
- Logic is common for all servlets
- Request should be blocked before reaching Spring
- Security, logging, CORS, compression

Use INTERCEPTOR when:
- Logic is Spring MVC specific
- Access to HandlerMethod / Controller
- Role-based authorization at controller level

------------------------------------------------
9. One-Line Summary (Interview Gold)
------------------------------------------------
Filters work at Servlet container level before DispatcherServlet,
while Interceptors work at Spring MVC level around controller execution.
