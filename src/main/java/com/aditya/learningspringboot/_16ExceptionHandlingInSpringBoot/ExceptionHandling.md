===============================
HOW SPRING BOOT HANDLES EXCEPTIONS (INTERNAL FLOW)
===============================

When **your controller / service throws an exception**, Spring Boot follows a **well-defined chain**
to convert that exception into an HTTP response.

------------------------------------------------
HIGH LEVEL FLOW
------------------------------------------------

Controller / Service throws Exception
↓
DispatcherServlet
↓
HandlerExceptionResolverComposite (ORCHESTRATOR)
↓
Tries resolvers in ORDER:
1) ExceptionHandlerExceptionResolver
2) ResponseStatusExceptionResolver
3) DefaultHandlerExceptionResolver
   ↓
   If none handled → DefaultErrorAttributes
   ↓
   /error endpoint → ErrorController → HTTP Response

================================================
1. HandlerExceptionResolverComposite (ORCHESTRATOR)
   ================================================

• This is the **central coordinator**
• It does NOT handle exceptions itself
• It delegates to multiple resolvers **in a fixed order**
• Stops at the FIRST resolver that can handle the exception

Resolvers inside it (order matters):

1. ExceptionHandlerExceptionResolver
2. ResponseStatusExceptionResolver
3. DefaultHandlerExceptionResolver

Code (conceptual):

public class HandlerExceptionResolverComposite {

    List<HandlerExceptionResolver> resolvers;

    public ModelAndView resolveException(...) {
        for (HandlerExceptionResolver resolver : resolvers) {
            ModelAndView mav = resolver.resolveException(...);
            if (mav != null) {
                return mav; // STOP here
            }
        }
        return null;
    }
}

================================================
2. ExceptionHandlerExceptionResolver
   (@ControllerAdvice + @ExceptionHandler)
   ================================================

• MOST POWERFUL and MOST USED
• Handles **application-level / custom exceptions**
• Works with:
- @ExceptionHandler
- @ControllerAdvice
- @RestControllerAdvice

Example:

// Custom Exception
public class UserNotFoundException extends RuntimeException {
public UserNotFoundException(String message) {
super(message);
}
}

// Global Exception Handler
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {

        ErrorResponse response = new ErrorResponse(
                "USER_NOT_FOUND",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }
}

Flow:
UserNotFoundException thrown
→ ExceptionHandlerExceptionResolver finds matching @ExceptionHandler
→ ResponseEntity returned
→ HTTP response sent
→ FLOW STOPS HERE

================================================
3. ResponseStatusExceptionResolver
   (@ResponseStatus)
   ================================================

• Used when exception class itself defines HTTP status
• Less flexible than @ExceptionHandler
• NO custom response body control (basic)

Example:

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOrderException extends RuntimeException {
public InvalidOrderException(String message) {
super(message);
}
}

Flow:
InvalidOrderException thrown
→ Resolver reads @ResponseStatus
→ HTTP 400 returned
→ Default error body generated
→ FLOW STOPS HERE

⚠️ Limitation:
- Cannot return structured custom JSON easily
- Not recommended for complex APIs

================================================
4. DefaultHandlerExceptionResolver
   (Spring Framework Exceptions)
   ================================================

• Handles **predefined Spring MVC exceptions**
• You normally DO NOT write code for this
• Examples it handles:

Exception → HTTP Status
-------------------------------------
HttpRequestMethodNotSupportedException → 405
MissingServletRequestParameterException → 400
HttpMediaTypeNotSupportedException → 415
TypeMismatchException → 400

Example scenario:
@RequestParam missing → Spring throws MissingServletRequestParameterException
→ DefaultHandlerExceptionResolver converts it to HTTP 400

================================================
5. DefaultErrorAttributes (LAST RESORT)
   ================================================

• Used ONLY when:
- No @ExceptionHandler found
- No @ResponseStatus found
- Not a known Spring exception

• DefaultErrorAttributes builds the error response model
• Data is exposed via "/error" endpoint

Default attributes generated:

{
"timestamp": "2025-12-27T09:10:00",
"status": 500,
"error": "Internal Server Error",
"message": "Something went wrong",
"path": "/api/users/1"
}

Key Method:

public Map<String, Object> getErrorAttributes(
WebRequest webRequest,
ErrorAttributeOptions options
)

You CAN override it:

@Component
public class CustomErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(
            WebRequest webRequest,
            ErrorAttributeOptions options) {

        Map<String, Object> attrs = super.getErrorAttributes(webRequest, options);
        attrs.put("appName", "User-Service");
        return attrs;
    }
}

================================================
SUMMARY (INTERVIEW GOLD)
================================================

Priority Order (VERY IMPORTANT):

1️⃣ @ExceptionHandler / @ControllerAdvice
2️⃣ @ResponseStatus
3️⃣ DefaultHandlerExceptionResolver
4️⃣ DefaultErrorAttributes (/error)

BEST PRACTICE:
✔ Use @RestControllerAdvice + @ExceptionHandler for APIs
❌ Avoid relying on DefaultErrorAttributes
❌ Avoid @ResponseStatus for complex error responses

ONE LINE:
HandlerExceptionResolverComposite is the ORCHESTRATOR,
ExceptionHandlerExceptionResolver is what YOU control,
DefaultErrorAttributes is the FINAL FALLBACK.

===============================
END
===============================
