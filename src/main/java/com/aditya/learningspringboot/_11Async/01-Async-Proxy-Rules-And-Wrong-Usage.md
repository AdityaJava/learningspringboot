================================================================================
SECTION 1: PROXY RULES, VISIBILITY & WRONG USAGE
================================================================================

1) DIFFERENT CLASS RULE (MOST IMPORTANT)

Spring implements @Async using AOP proxies.
If an @Async method is called from the SAME class, proxy interception is skipped.

❌ WRONG — Internal method call (Async IGNORED)

@Service
public class UserService {

    public void processUser() {
        // ❌ Internal call → NO proxy → runs synchronously
        sendEmailAsync();
    }

    @Async
    public void sendEmailAsync() {
        System.out.println("Email sent asynchronously");
    }
}

Result:
- No new thread
- @Async ignored
- Executes synchronously


2) PUBLIC METHOD RULE

Spring AOP intercepts ONLY public methods.

❌ WRONG

@Async
protected void processAsync() {
}

❌ WRONG

@Async
private void processAsync() {
}

✅ CORRECT

@Async
public void processAsync() {
}


3) INDUSTRY STANDARD DESIGN (CORRECT WAY)

Rule:
- @Async → ONLY thread switching
- @Transactional → ONLY database logic
- MUST be in DIFFERENT classes

------------------------------------------------

@Component
public class ClassA {

    private final UserService userService;

    public ClassA(UserService userService) {
        this.userService = userService;
    }

    public Void updateUserMethod() {
        userService.updateUser(); // Proxy-based async call
        return null;
    }
}

------------------------------------------------

@Service
public class UserService {

    private final UserUtility userUtility;

    public UserService(UserUtility userUtility) {
        this.userUtility = userUtility;
    }

    @Async
    public void updateUser() {
        userUtility.updateUser();
    }
}

------------------------------------------------

@Component
public class UserUtility {

    @Transactional
    public void updateUser() {
        // 1. update userStatus
        // 2. update user firstName
        // 3. update user
    }
}

Why this is BEST PRACTICE:
- Async proxy always works
- Transaction boundary is clear
- Rollback behavior is predictable
- Clean separation of concerns


