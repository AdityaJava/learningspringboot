============================================================
HATEOAS — COMPLETE NOTES + EXAMPLE + CODE (ONE WINDOW)
============================================================

1️⃣ WHAT IS HATEOAS?
-------------------
HATEOAS stands for:
➡️ Hypermedia As The Engine Of Application State

Definition:
HATEOAS is a REST constraint where the server drives the client’s interaction
by providing links (hypermedia) in the API response.

In simple words:
👉 Client should NOT hardcode API URLs
👉 Client should discover available actions dynamically from response links

HATEOAS response answers:
- What data did I get?
- What can I do next?
- Where should I go next?

------------------------------------------------------------

2️⃣ GOOD REAL-WORLD ANALOGY
--------------------------
Think of an ATM screen:
- You insert card
- Screen shows options: Withdraw, Balance, Deposit
- You do NOT need to know internal ATM flows

Similarly:
- API response shows links
- Client chooses next action based on links

------------------------------------------------------------

3️⃣ HOW WILL A RESPONSE LOOK WITH HATEOAS?
------------------------------------------

WITHOUT HATEOAS ❌
------------------
{
"id": 101,
"name": "Aditya",
"email": "aditya@gmail.com"
}

Problems:
- Client must hardcode URLs
- Tight coupling
- No API discovery

------------------------------------------------------------

WITH HATEOAS ✅
--------------
{
"id": 101,
"name": "Aditya",
"email": "aditya@gmail.com",
"_links": {
"self": {
"href": "/users/101"
},
"update": {
"href": "/users/101"
},
"delete": {
"href": "/users/101"
},
"all-users": {
"href": "/users"
}
}
}

Meaning:
- Client knows exactly what actions are allowed
- URLs can change without breaking clients

------------------------------------------------------------

4️⃣ PURPOSE OF HATEOAS
---------------------

✔ Loose Coupling
- Client does NOT depend on fixed URLs

✔ API Discovery
- Client learns next possible actions from response

✔ Evolvable APIs
- Backend URLs can change safely

✔ True REST Compliance
- REST is not just HTTP + JSON
- Hypermedia is mandatory for REST maturity

------------------------------------------------------------

5️⃣ WHAT NOT TO DO ❌
-------------------

❌ Do NOT hardcode URLs on client
❌ Do NOT return plain DTOs when HATEOAS is required
❌ Do NOT expose internal microservice structure
❌ Do NOT overuse links for every tiny thing
❌ Do NOT mix business logic into HATEOAS layer

Rule:
👉 Links describe navigation, NOT business rules

------------------------------------------------------------

6️⃣ COMPLETE SPRING BOOT HATEOAS CODE
------------------------------------

DEPENDENCY (pom.xml)
--------------------
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-hateoas</artifactId>
</dependency>

------------------------------------------------------------

ENTITY
------
public class User {
private Long id;
private String name;
private String email;

    // getters & setters
}

------------------------------------------------------------

CONTROLLER WITH HATEOAS
----------------------
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/{id}")
    public EntityModel<User> getUserById(@PathVariable Long id) {

        User user = new User();
        user.setId(id);
        user.setName("Aditya");
        user.setEmail("aditya@gmail.com");

        EntityModel<User> resource = EntityModel.of(user);

        resource.add(linkTo(methodOn(UserController.class)
                .getUserById(id)).withSelfRel());

        resource.add(linkTo(methodOn(UserController.class)
                .getAllUsers()).withRel("all-users"));

        resource.add(linkTo(methodOn(UserController.class)
                .deleteUser(id)).withRel("delete"));

        resource.add(linkTo(methodOn(UserController.class)
                .updateUser(id, null)).withRel("update"));

        return resource;
    }

    @GetMapping
    public String getAllUsers() {
        return "All users";
    }

    @PutMapping("/{id}")
    public String updateUser(@PathVariable Long id,
                             @RequestBody User user) {
        return "User updated";
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        return "User deleted";
    }
}

------------------------------------------------------------

7️⃣ KEY INTERVIEW TAKEAWAYS 🔥
------------------------------
✔ HATEOAS enables loose coupling
✔ Client discovers API via links
✔ URLs can change safely
✔ Required for Level-3 REST maturity
✔ Implemented using Spring HATEOAS

------------------------------------------------------------
END
------------------------------------------------------------
