================================================================================
SECTION 4: ASYNC EXCEPTION HANDLING
================================================================================

1) ASYNC METHOD WITH RETURN VALUE

Exception handled at .get()

------------------------------------------------

try {
String result = asyncService.doTask().get();
} catch (Exception e) {
// handle exception
}


2) ASYNC METHOD WITH VOID RETURN TYPE

Exceptions are NOT propagated to caller

------------------------------------------------

@Async
public void process() {
throw new RuntimeException("Boom");
}


3) TRY-CATCH INSIDE ASYNC (NOT RECOMMENDED)

------------------------------------------------

@Async
public void process() {
try {
riskyOperation();
} catch (Exception e) {
log.error("Async failed", e);
}
}

Problem:
- Pollutes business logic
- Not scalable


4) INDUSTRY STANDARD — Custom AsyncExceptionHandler

STEP 1: Custom Exception Handler

------------------------------------------------

@Component
public class CustomAsyncExceptionHandler
implements AsyncUncaughtExceptionHandler {

    @Override
    public void handleUncaughtException(
            Throwable ex,
            Method method,
            Object... params) {

        System.out.println(
            "Exception in async method: " + method.getName()
        );
        ex.printStackTrace();
    }
}

------------------------------------------------

STEP 2: Async Configuration

------------------------------------------------

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private final CustomAsyncExceptionHandler exceptionHandler;

    public AsyncConfig(CustomAsyncExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public Executor getAsyncExecutor() {
        return Executors.newFixedThreadPool(5);
    }

    @Override
    public AsyncUncaughtExceptionHandler
           getAsyncUncaughtExceptionHandler() {
        return exceptionHandler;
    }
}

Benefits:
- Centralized async error handling
- Clean business code
- Production-grade observability