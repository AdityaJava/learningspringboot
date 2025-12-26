/***************************************************************************************************
🔥 SPRING BOOT — FILTERS vs MULTIPLE INTERCEPTORS (CLEAN, STUDENT-SAFE EXAMPLE) 🔥
***************************************************************************************************

====================================================================================
1️⃣ BIG PICTURE (READ THIS FIRST)
====================================================================================

FILTER:
- Runs BEFORE DispatcherServlet
- Part of Servlet layer
- Used for HTTP-level, application-wide concerns

INTERCEPTOR:
- Runs AFTER DispatcherServlet
- Part of Spring MVC
- Used for Controller-level, business-aware concerns

🧠 Mental Model:
----------------
FILTER       → "Should this request ENTER the application?"
INTERCEPTOR  → "How should this request be HANDLED by controllers?"

====================================================================================
2️⃣ NON-SECURITY EXAMPLE USED HERE (NO CONFUSION)
====================================================================================

FILTER USE CASE:
✔ Request Correlation ID generation
✔ Global logging of request URI

INTERCEPTOR USE CASE:
✔ Controller execution timing
✔ Controller-specific logging
✔ Business-layer metrics

(NO authentication, NO authorization)

====================================================================================
3️⃣ FILTER EXAMPLE — RequestCorrelationIdFilter
====================================================================================

public class RequestCorrelationIdFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String correlationId = UUID.randomUUID().toString();
        httpRequest.setAttribute("CORRELATION_ID", correlationId);

        System.out.println("FILTER - CorrelationId: " + correlationId +
                           " URI: " + httpRequest.getRequestURI());

        chain.doFilter(request, response);
    }
}

====================================================================================
4️⃣ WHY THIS LOGIC BELONGS IN FILTER
====================================================================================
✔ Applies to ALL requests
✔ Independent of controllers
✔ No need to know which controller is called
✔ Pure HTTP-level concern

====================================================================================
5️⃣ MULTIPLE INTERCEPTORS — CONTROLLER-LEVEL EXAMPLES
====================================================================================

------------------------------------------------
🔹 LoggingInterceptor
------------------------------------------------
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        System.out.println("LoggingInterceptor - preHandle");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {

        System.out.println("LoggingInterceptor - afterCompletion");
    }
}

------------------------------------------------
🔹 PerformanceInterceptor
------------------------------------------------
public class PerformanceInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        request.setAttribute("START_TIME", System.currentTimeMillis());
        System.out.println("PerformanceInterceptor - preHandle");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {

        long startTime = (long) request.getAttribute("START_TIME");
        long timeTaken = System.currentTimeMillis() - startTime;

        System.out.println("PerformanceInterceptor - Time Taken: " + timeTaken + " ms");
    }
}

------------------------------------------------
🔹 BusinessAuditInterceptor
------------------------------------------------
public class BusinessAuditInterceptor implements HandlerInterceptor {

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) {

        System.out.println("BusinessAuditInterceptor - postHandle");
    }
}

====================================================================================
6️⃣ REGISTER MULTIPLE INTERCEPTORS (ORDER MATTERS ❗)
====================================================================================

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new LoggingInterceptor())
                .addPathPatterns("/api/**");

        registry.addInterceptor(new PerformanceInterceptor())
                .addPathPatterns("/api/**");

        registry.addInterceptor(new BusinessAuditInterceptor())
                .addPathPatterns("/api/**");
    }
}

====================================================================================
7️⃣ REQUEST FLOW — EXECUTION ORDER
====================================================================================

Client Request
|
v
LoggingInterceptor        → preHandle()
|
PerformanceInterceptor   → preHandle()
|
BusinessAuditInterceptor → preHandle()
|
Controller

✔ preHandle() → TOP to BOTTOM (Registration Order)

====================================================================================
8️⃣ RESPONSE FLOW — EXECUTION ORDER
====================================================================================

Controller
|
v
BusinessAuditInterceptor → postHandle() / afterCompletion()
|
PerformanceInterceptor   → postHandle() / afterCompletion()
|
LoggingInterceptor        → postHandle() / afterCompletion()
|
Client

✔ postHandle() & afterCompletion() → BOTTOM to TOP

====================================================================================
9️⃣ COMPLETE FLOW (FILTER + INTERCEPTORS)
====================================================================================

Client
|
v
RequestCorrelationIdFilter (FILTER)
|
v
DispatcherServlet
|
v
LoggingInterceptor        → preHandle()
|
PerformanceInterceptor   → preHandle()
|
BusinessAuditInterceptor → preHandle()
|
Controller
|
BusinessAuditInterceptor → postHandle() / afterCompletion()
|
PerformanceInterceptor   → postHandle() / afterCompletion()
|
LoggingInterceptor        → postHandle() / afterCompletion()
|
Client

====================================================================================
🔟 GOLDEN RULES (STUDENT-FRIENDLY)
====================================================================================

✔ FILTER → HTTP-level, application-wide logic
✔ INTERCEPTOR → Controller-level, business-aware logic
✔ REQUEST  → Same order as registration
✔ RESPONSE → Reverse order of registration

🧠 Memory Trick:
----------------
"FILTER guards the gate,
INTERCEPTORS guide the request inside."

====================================================================================
🔥 END — CLEAN, SAFE & INTERVIEW-READY 🔥
***************************************************************************************************
