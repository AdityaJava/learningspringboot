# HTTP Response Status Codes – Detailed Notes (with Examples)

| Category | Code Range | Meaning | When It Is Used | Common Status Codes | Example Scenario |
|--------|------------|---------|----------------|--------------------|------------------|
| **1XX – Informational** | 100–199 | Request received, processing is continuing | Rarely used directly in REST APIs; mostly used internally by HTTP clients/servers | `100 Continue`, `101 Switching Protocols` | Client sends large payload → server replies **100 Continue** to indicate it’s safe to send the body |
| **2XX – Success** | 200–299 | Request was successfully received, understood, and processed | Used when API call completes without errors | `200 OK`, `201 Created`, `202 Accepted`, `204 No Content` | `POST /users` → **201 Created** after successfully creating a user |
| **3XX – Redirection** | 300–399 | Client must take additional action to complete the request | Mostly avoided in REST APIs; more common in browsers | `301 Moved Permanently`, `302 Found`, `304 Not Modified` | Cached response used → **304 Not Modified** returned |
| **4XX – Client / Validation Error** | 400–499 | Client sent an invalid request | Used when request input, headers, auth, or payload is incorrect | `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, `422 Unprocessable Entity` | Missing mandatory field → **400 Bad Request** |
| **5XX – Server Error** | 500–599 | Server failed to process a valid request | Used when something breaks on server side | `500 Internal Server Error`, `502 Bad Gateway`, `503 Service Unavailable`, `504 Gateway Timeout` | NullPointerException in service layer → **500 Internal Server Error** |

---

## Common REST API Examples

### ✅ 2XX – Success Example
```http
GET /api/users/1
HTTP/1.1 200 OK
```

### ❌ 4XX – Validation Error Example
```http
POST /api/users
HTTP/1.1 400 Bad Request

{
  "error": "email is mandatory"
}
```


### ❌ 5XX – Server Error Example
```http
GET /api/orders/99
HTTP/1.1 500 Internal Server Error

{
"error": "Unexpected error occurred"
}
```

### Quick Thumb Rule (Interview Friendly)
```
If the client can fix the request → 4XX
If the server must be fixed → 5XX
```