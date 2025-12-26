# Spring Boot Interceptors – Industry-Standard Deep Dive

---

## 1️⃣ What is an Interceptor?

An **Interceptor** is a Spring MVC component used to intercept HTTP requests **before and after controller execution**. It is a powerful tool for cross-cutting concerns that should not pollute business logic.

### Common Use Cases:
* **Authentication & Authorization:** Verifying tokens or session headers.
* **Logging & Auditing:** Tracking who requested what and when.
* **Metrics Collection:** Measuring controller execution time.
* **Request Tracing:** Injecting or validating Correlation IDs (e.g., `X-Request-ID`).

### Key Characteristics:
* **Layer Specific:** Works **only at the Spring MVC (Controller) layer**.
* **Context Aware:** Has access to the `HttpServletRequest`, `HttpServletResponse`, and the `handler` (the actual Controller method).
* **Granular Control:** Can be mapped to specific URL patterns.

> **Note:** Interceptors do **not** intercept Filters or static resources by default. If you need to manipulate requests before they reach the `DispatcherServlet`, use a **Servlet Filter**.

---

## 2️⃣ The Request Lifecycle

Understanding the sequence of execution is critical for debugging and architectural design.



**The Flow:**
1.  **Incoming Request**
2.  `preHandle()`: Boolean check. If `false`, the execution chain stops.
3.  **Controller Method**: Your business logic.
4.  `postHandle()`: Executed after the controller but before the view is rendered.
5.  **Response Commit**: The view is rendered or JSON is written to the buffer.
6.  `afterCompletion()`: Executed after the entire request is finished. Great for resource cleanup.

---

## 3️⃣ Implementation: HandlerInterceptor

Interceptors are created by implementing the `HandlerInterceptor` interface.

### Interceptor Implementation
```java
package com.example.demo.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class UserControllerInterceptor implements HandlerInterceptor {

    /**
     * preHandle() - Called BEFORE controller execution.
     * Return true to continue, false to block the request.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, 
                             HttpServletResponse response, 
                             Object handler) throws Exception {

        String requestId = request.getHeader("X-REQUEST-ID");

        if (requestId == null || requestId.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Error: Missing X-REQUEST-ID header");
            return false; // Interrupts the request
        }

        System.out.println("PreHandle: Validated Request ID " + requestId);
        return true; 
    }

    /**
     * postHandle() - Called AFTER controller logic but BEFORE response commit.
     */
    @Override
    public void postHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler, 
                           ModelAndView modelAndView) {
        System.out.println("PostHandle: Logic executed successfully.");
    }

    /**
     * afterCompletion() - Called AFTER response is fully committed.
     * Best for cleanup and logging exceptions.
     */
    @Override
    public void afterCompletion(HttpServletRequest request, 
                                HttpServletResponse response, 
                                Object handler, 
                                Exception ex) {
        System.out.println("AfterCompletion: Request lifecycle finished.");
        if (ex != null) {
            System.err.println("Exception caught in interceptor: " + ex.getMessage());
        }
    }
}

4️⃣ Registration: WebMvcConfigurer
package com.example.demo.config;

import com.example.demo.interceptor.UserControllerInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserControllerInterceptor userControllerInterceptor;

    public WebMvcConfig(UserControllerInterceptor userControllerInterceptor) {
        this.userControllerInterceptor = userControllerInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userControllerInterceptor)
                // Apply only to User-related APIs
                .addPathPatterns("/api/users/**")
                // Exclude public endpoints and system tools
                .excludePathPatterns(
                        "/api/users/public/**",
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                );
    }
}

## 🔄 Comparison Table

| Feature             | `postHandle`                                                                 | `afterCompletion`                                                      |
|---------------------|------------------------------------------------------------------------------|----------------------------------------------------------------------|
| **Execution Time**   | After controller execution, but **before** the view is rendered.             | After the entire request is finished and the **response is sent** to the client. |
| **Response State**   | **Not committed.** You can still modify the Model or the response.            | **Already committed.** The response is read-only.                     |
| **Modify Model**     | ✅ Yes                                                                       | ❌ No                                                                 |
| **Exception Handling**| ❌ No (Does not run if an exception occurs in the controller).               | ✅ Yes (Provides access to the `Exception` object for logging).       |
| **Primary Use**      | Enriching the Model, adding common attributes, or post-processing the response.| Cleanup of resources, request logging, and performance metrics.       |

---

## 🔥 Industry Best Practices

### 1. Single Responsibility Principle
Keep your interceptors focused. One interceptor should perform one specific task (e.g., a `LoggingInterceptor` for logs and an `AuthInterceptor` for security) rather than a single "God Interceptor."

### 2. Fail Fast
Whenever possible, perform validation and security checks in the `preHandle` method. This prevents the application from wasting CPU cycles and memory on requests that are destined to be rejected.

### 3. Path Precision
Be specific with your configurations. Intercepting every request (`/**`) can significantly degrade performance by processing static assets (images, CSS) or health check endpoints.
- Use `addPathPatterns` for target routes.
- Use `excludePathPatterns` for static resources and public APIs.

### 4. Avoid Business Logic
Interceptors are part of the web infrastructure layer. Never place core business rules or database transactions inside an interceptor; delegate those responsibilities to the **Service Layer**.

### 5. Thread Safety
**Interceptors are Singletons.** Because a single instance is shared across all requests, never use instance variables to store request-specific data.
- **Do:** Use `HttpServletRequest.setAttribute()` or `ThreadLocal`.
- **Don't:** Declare private variables to hold user data.
