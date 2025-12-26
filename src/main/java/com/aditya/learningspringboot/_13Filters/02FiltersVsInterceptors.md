# 🌐 Spring Boot Filters vs Interceptors — Clear, Practical Notes (COPY-READY)

---

## 1️⃣ What are Filters?

**Filters** are part of the **Servlet specification (javax.servlet.Filter)**.

They:
- Sit **before any servlet** is invoked
- Intercept **every HTTP request and response**
- Work at the **web container level** (Tomcat/Jetty), not Spring MVC level

In **:contentReference[oaicite:0]{index=0}**, there is effectively **only one servlet**:
👉 `DispatcherServlet`

So:
Client Request
↓
[ FILTER ]
↓
DispatcherServlet
↓
Controller


### ✅ Key Characteristics of Filters
~~- Independent of Spring MVC
- Executed **even before Spring context**~~
- Can block or modify requests early
- Cannot access:
    - Controller method
    - HandlerMethod
    - @RequestMapping details

### 🔥 Typical Use-Cases
- Authentication (Spring Security Filter Chain)
- Logging raw request/response
- CORS
- Request/Response wrapping
- Header manipulation
- XSS protection
- GZIP compression

---

## 2️⃣ DispatcherServlet Invocation in Spring Boot

In Spring Boot:
- **DispatcherServlet is always invoked**
- It acts as the **Front Controller**
- All REST APIs, MVC controllers pass through it

Flow:
Client
↓
Filter(s)
↓
DispatcherServlet ← Entry point to Spring MVC
↓
Interceptor (preHandle)
↓
Controller
↓
Interceptor (postHandle / afterCompletion)


⚠️ Filters run **BEFORE DispatcherServlet**
⚠️ Interceptors run **INSIDE DispatcherServlet lifecycle**

---

## 3️⃣ Why Filters Contain Common Logic for All Servlets?

Servlet containers were designed to support:
- Multiple servlets
- Common cross-cutting concerns

Even though Spring Boot has **only one servlet (DispatcherServlet)**,
Filters are still used for **cross-application concerns**.

### Example: Spring Security
- Uses **FilterChain**
- Authentication happens **before request reaches controller**
- Blocks unauthorized requests early (performance + security)

💡 Rule:
> If logic applies to **every HTTP request**, use a **Filter**

---

## 4️⃣ REST API–Specific Logic → Interceptors

**Interceptors** are Spring MVC components:
- Implement `HandlerInterceptor`
- Execute **after DispatcherServlet**
- Work at **Controller & REST API level**

They have access to:
- HandlerMethod
- Controller class & method
- Annotations
- Path variables
- Request mapping info

### 🔥 Ideal Use-Cases for Interceptors
- REST API authentication/authorization
- API-specific logging
- Audit logging
- Rate limiting per endpoint
- Validation based on controller annotations
- Performance metrics per API
- Tenant resolution for REST APIs

💡 Rule:
> If logic depends on **controller or API behavior**, use an **Interceptor**

---

## 5️⃣ ✋ GOLDEN HAND RULE (VERY IMPORTANT)

### 🟢 Use **FILTER** when:
✔ Logic is **HTTP / protocol level**
✔ Applies to **all requests**
✔ Must run **before Spring MVC**
✔ Does NOT depend on controller
✔ Works on headers, cookies, raw body

📌 Examples:
- Authentication at gateway level
- CORS
- Security
- Logging raw requests
- Encoding, compression

---

### 🔵 Use **INTERCEPTOR** when:
✔ Logic is **REST / Controller specific**
✔ Needs access to:
- Controller
- Method
- Annotations
  ✔ Should run **after DispatcherServlet**
  ✔ Depends on API behavior

📌 Examples:
- API authorization
- User role validation
- API metrics
- Annotation-based checks
- Audit logging per endpoint

---

## 6️⃣ One-Line Memory Trick 🧠

> **Filter = HTTP-level (Before DispatcherServlet)**  
> **Interceptor = REST-level (Inside DispatcherServlet)**

OR

> **Filter → Technical concern**  
> **Interceptor → Business / API concern**

---

## 7️⃣ Final Visual Summary

Client Request
↓
[ FILTER ] → HTTP / Security / Common Logic
↓
DispatcherServlet
↓
[ INTERCEPTOR ] → REST / Controller Logic
↓
Controller

