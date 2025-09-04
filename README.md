# scala-play-framework-workshop

## CSRF (Cross-Site Request Forgery)

CSRF is a common web security vulnerability where an attacker tricks a logged-in user’s browser into sending unwanted requests to another site where the user is authenticated.  
Example: if you are logged into your bank and visit a malicious site, it could make your browser submit a hidden money transfer request to the bank using your valid session cookie.

### How to prevent it
- **CSRF tokens**: The server generates a random token and embeds it in forms. When the form is submitted, the token must match what the server expects.
- **SameSite cookies**: Restrict cookies from being sent with cross-site requests.
- **Play Framework**: Play includes built-in CSRF protection. When you use `@helper.form`, a hidden CSRF token field is automatically added and validated on submission.

## Session vs Flash (Play Framework)

- **Session**
  - `Map[String, String]` (cookie-based)
  - Persists across multiple requests
  - Usage: user info (username, role), language, preferences
  - Cleared manually with `.withNewSession` or overwrite

- **Flash**
  - `Map[String, String]` (cookie-based)
  - Lives only for the **next request**, then auto-cleared
  - Usage: one-time messages (success, error, logout notice)
  - Automatically cleared after being read once
 
## What is `implicit request` in Play Framework?

### 1. The `request`
When a user visits your app (e.g., `GET /login`), Play creates a **`request` object**.  
This object contains everything about the HTTP request:
- Method (GET, POST, …)
- URL and query parameters
- Headers and cookies
- Session and Flash data

Think of it as the **envelope** for the incoming request.

---

### 2. The word `implicit`
In Scala, `implicit` means **“automatically provide this value if needed.”**

Normally, if a function needs `request`, you would pass it by hand.  
But if you write `implicit request`, Play/Scala will automatically supply it to any function that needs it.

---

### 3. Example in a Controller
Without `implicit`:
```scala
def taskList = Action { request: RequestHeader =>
  Ok(views.html.taskList()(request)) // you must pass request manually
}



