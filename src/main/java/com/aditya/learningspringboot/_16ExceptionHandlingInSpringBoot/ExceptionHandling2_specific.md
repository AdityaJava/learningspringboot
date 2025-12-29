================================================================================
SPRING BOOT – EXCEPTION HANDLING FLOW (DEEP DIVE NOTES + CODE)
================================================================================

WHEN WE RETURN / THROW OUR OWN EXCEPTION – HOW SPRING HANDLES IT
--------------------------------------------------------------------------------

Whenever an exception is thrown from:
- Controller
- Service
- Repository

Spring MVC delegates exception handling to:

HandlerExceptionResolverComposite (ORCHESTRATOR)

This composite internally maintains an ORDERED LIST of resolvers and tries them
ONE BY ONE until one of them HANDLES the exception.

Default Order:
1. ExceptionHandlerExceptionResolver
2. ResponseStatusExceptionResolver
3. DefaultHandlerExceptionResolver

If NONE of the above handles it → control goes to /error endpoint
→ handled by BasicErrorController using DefaultErrorAttributes

================================================================================
WHAT DOES "RESOLVER HANDLES / RESOLVES" MEAN?
================================================================================

A resolver is said to have HANDLED an exception if it:

✔ Converts Exception → HTTP Response
✔ Sets HTTP Status
✔ Writes response body (JSON / text)
✔ Prevents exception from propagating further

Once handled → remaining resolvers are SKIPPED

================================================================================
1. ExceptionHandlerExceptionResolver
   ================================================================================

PURPOSE:
--------
Handles exceptions using:
- @ExceptionHandler
- @ControllerAdvice

This is the MOST POWERFUL and MOST USED resolver.

It looks for:
1. @ExceptionHandler method in SAME controller (HIGHEST PRIORITY)
2. @ExceptionHandler method in @ControllerAdvice (GLOBAL)

----------------------------------------
WHAT PARAMETERS @ExceptionHandler CAN TAKE
----------------------------------------

@ExceptionHandler method can accept:

- Exception / CustomException
- HttpServletRequest
- HttpServletResponse
- WebRequest
- Locale
- Principal
- Model
- BindingResult
- Any argument supported by HandlerMethod

Return Types:
- ResponseEntity<?>
- Object (converted to JSON)
- String (view name)
- void (manual response writing)

----------------------------------------
CONTROLLER LEVEL EXCEPTION HANDLER
----------------------------------------

Controller-level handler has HIGHER PRIORITY than global handler.

@RestController
@RequestMapping("/accounts")
public class BankController {

    @GetMapping("/{id}")
    public String getAccount(@PathVariable Long id) {
        if (id == 1) {
            throw new InsufficientBalanceException("Balance is low");
        }
        return "OK";
    }

    // CONTROLLER LEVEL HANDLER
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<String> handleInsufficientBalance(
            InsufficientBalanceException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }
}

----------------------------------------
GLOBAL EXCEPTION HANDLING (ControllerAdvice)
----------------------------------------

PROBLEM WITHOUT @ControllerAdvice:
- Same exception handling logic duplicated across controllers

SOLUTION:
- @ControllerAdvice + @ExceptionHandler

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<String> handleInsufficientBalance(
            InsufficientBalanceException ex,
            HttpServletRequest request) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("GLOBAL HANDLER: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Something went wrong");
    }
}

PRIORITY RULE:
---------------
Controller-level @ExceptionHandler > Global @ControllerAdvice

================================================================================
WRITING RESPONSE DIRECTLY USING HttpServletResponse
================================================================================

@RestController
public class ManualResponseController {

    @GetMapping("/manual")
    public void manualException(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Manual response\"}");
    }
}

================================================================================
2. ResponseStatusExceptionResolver
   ================================================================================

PURPOSE:
--------
Handles exceptions annotated with @ResponseStatus

IMPORTANT CLARIFICATION (CORRECTED UNDERSTANDING):
--------------------------------------------------
✔ Resolver runs BEFORE DefaultHandlerExceptionResolver
✔ It CHECKS whether the thrown exception class has @ResponseStatus
✔ If YES → converts exception to HTTP response
✔ If NO → moves to next resolver

This resolver DOES NOT use @ExceptionHandler methods.

----------------------------------------
@ResponseStatus – WHY ANNOTATION?
----------------------------------------

- Declarative HTTP status mapping
- No need to write handler method
- Clean for simple exceptions

----------------------------------------
EXAMPLE – CUSTOM EXCEPTION WITH @ResponseStatus
----------------------------------------

@ResponseStatus(
value = HttpStatus.NOT_FOUND,
reason = "Account not found"
)
public class AccountNotFoundException extends RuntimeException {
}

@RestController
public class AccountController {

    @GetMapping("/account/{id}")
    public String getAccount(@PathVariable Long id) {
        if (id == 99) {
            throw new AccountNotFoundException();
        }
        return "Account Found";
    }
}

RESPONSE:
---------
HTTP/1.1 404
Body:
Account not found

----------------------------------------
ResponseStatusException (PROGRAMMATIC)
----------------------------------------

@GetMapping("/account2/{id}")
public String getAccount2(@PathVariable Long id) {
if (id == 99) {
throw new ResponseStatusException(
HttpStatus.NOT_FOUND,
"Account missing"
);
}
return "OK";
}

================================================================================
3. DefaultHandlerExceptionResolver
   ================================================================================

PURPOSE:
--------
Handles ONLY Spring MVC / Framework exceptions

YOU SHOULD NOT WRITE CODE FOR THIS

This is Spring-internal fallback resolver.

----------------------------------------
COMMON EXCEPTIONS HANDLED
----------------------------------------

- HttpRequestMethodNotSupportedException → 405
- HttpMediaTypeNotSupportedException → 415
- MissingServletRequestParameterException → 400
- TypeMismatchException → 400
- NoHandlerFoundException → 404
- MethodArgumentNotValidException → 400

----------------------------------------
EXAMPLE
----------------------------------------

POST /accounts (but only GET exists)

Exception Thrown:
HttpRequestMethodNotSupportedException

Handled Automatically by:
DefaultHandlerExceptionResolver

Response:
---------
HTTP 405 METHOD NOT ALLOWED

================================================================================
WHAT IF NONE OF THE RESOLVERS HANDLE THE EXCEPTION?
================================================================================

Then request is forwarded to:

/error  →  BasicErrorController

Uses:
DefaultErrorAttributes#getErrorAttributes()

This builds default error JSON:
{
"timestamp": "...",
"status": 500,
"error": "Internal Server Error",
"path": "/api/test"
}

================================================================================
SUMMARY FLOW (IMPORTANT FOR INTERVIEWS)
================================================================================

Exception Thrown
↓
HandlerExceptionResolverComposite
↓
ExceptionHandlerExceptionResolver
↓
ResponseStatusExceptionResolver
↓
DefaultHandlerExceptionResolver
↓
/error → BasicErrorController → DefaultErrorAttributes

================================================================================
KEY TAKEAWAYS
================================================================================

✔ Controller-level @ExceptionHandler has highest priority
✔ @ControllerAdvice prevents code duplication
✔ @ResponseStatus is declarative & simple
✔ DefaultHandlerExceptionResolver handles framework errors only
✔ DefaultErrorAttributes is FINAL fallback

================================================================================
