================================================================================
SECTION 2: ASYNC + TRANSACTION MANAGEMENT
================================================================================

CASE 1: Transaction context DOES NOT propagate to @Async

Transaction is THREAD-BOUND.
@Async runs in a NEW thread.

------------------------------------------------

@Service
public class OrderService {

    @Transactional
    public void placeOrder() {
        paymentService.processPaymentAsync();
        throw new RuntimeException("Order failed");
    }
}

------------------------------------------------

@Service
public class PaymentService {

    @Async
    public void processPaymentAsync() {
        savePayment(); // COMMITTED
    }
}

Result:
- Order rolls back
- Payment STILL commits
- Rollback DOES NOT affect async method


CASE 2: @Transactional on @Async method

------------------------------------------------

@Async
@Transactional
public void processPaymentAsync() {
savePayment();
}

Behavior:
- Runs in a NEW transaction
- Rollback works INSIDE async
- Caller transaction is NOT copied
- Two independent transactions

⚠️ Use with caution


# CASE 3: INDUSTRY STANDARD (RECOMMENDED) — @Async + @Transactional

================================================================================
PROBLEM THIS PATTERN SOLVES
================================================================================

1) @Async creates a NEW thread → caller transaction is NOT propagated
2) @Transactional is THREAD-BOUND
3) Mixing @Async and @Transactional on the SAME method causes:
    - Unclear rollback behavior
    - Multiple hidden transactions
    - Hard-to-debug production issues

Therefore, INDUSTRY STANDARD is:
- @Async → ONLY responsibility = thread switch
- @Transactional → ONLY responsibility = DB consistency
- BOTH MUST BE IN DIFFERENT CLASSES

================================================================================
ARCHITECTURE OVERVIEW
================================================================================

Controller / Caller Thread
|
v
@Async Service (Thread Switch)
|
v
@Transactional Utility (DB Boundary)

================================================================================
COMPLETE WORKING CODE (PRODUCTION SAFE)
================================================================================

------------------------------------------------
CALLER CLASS (REQUEST / BUSINESS FLOW)
------------------------------------------------

@Component
public class ClassA {

    private final UserService userService;

    public ClassA(UserService userService) {
        this.userService = userService;
    }

    public void updateUserMethod() {

        // 1️⃣ Runs in caller thread
        // 2️⃣ Calls async proxy method
        // 3️⃣ Returns immediately (non-blocking)

        userService.updateUser();
    }
}

------------------------------------------------
ASYNC SERVICE (THREAD SWITCH ONLY)
------------------------------------------------

@Service
public class UserService {

    private final UserUtility userUtility;

    public UserService(UserUtility userUtility) {
        this.userUtility = userUtility;
    }

    @Async
    public void updateUser() {

        // 1️⃣ New thread starts here
        // 2️⃣ NO transaction here
        // 3️⃣ Only delegates work

        userUtility.updateUser();
    }
}

------------------------------------------------
TRANSACTIONAL UTILITY (DATABASE CONSISTENCY)
------------------------------------------------

@Component
public class UserUtility {

    @Transactional
    public void updateUser() {

        // 1️⃣ Transaction STARTS here
        // 2️⃣ All DB operations share SAME transaction
        // 3️⃣ If ANY exception occurs → FULL rollback

        // Example DB operations
        updateUserStatus();
        updateUserFirstName();
        saveUser();
    }

    private void updateUserStatus() {
        // DB update
    }

    private void updateUserFirstName() {
        // DB update
    }

    private void saveUser() {
        // DB save
    }
}

================================================================================
WHAT HAPPENS AT RUNTIME (STEP-BY-STEP)
================================================================================

1) HTTP request hits controller / caller
2) Caller invokes userService.updateUser()
3) Spring AOP proxy intercepts @Async
4) New thread is taken from async thread pool
5) userUtility.updateUser() is executed in new thread
6) @Transactional starts NEW transaction
7) All DB updates execute inside same transaction
8) If exception occurs → rollback
9) Thread completes → returned to pool

================================================================================
WHY THIS IS INDUSTRY STANDARD
================================================================================

Async Thread Safety
- Each request gets its own thread
- No shared mutable state

Transaction Safety
- Single clear transaction boundary
- Predictable rollback behavior

Clear Ownership
- @Async → threading concern
- @Transactional → persistence concern

Production Safe
- Easy debugging
- Observability-friendly
- No hidden transactions
- No proxy bypass

================================================================================
WHY NOT @Async + @Transactional TOGETHER?
================================================================================

❌ @Async
❌ @Transactional
public void updateUser() { }

Problems:
- Two responsibilities in one method
- New transaction ALWAYS created
- Caller transaction NEVER reused
- Hard to reason about rollback

================================================================================
FINAL GOLDEN RULE
================================================================================

✔ Never mix @Async and @Transactional on same method
✔ Always separate THREAD SWITCH and TRANSACTION BOUNDARY
✔ This pattern is used in real-world, high-scale Spring Boot systems

================================================================================
END
================================================================================
