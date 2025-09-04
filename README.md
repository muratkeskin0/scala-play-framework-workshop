# scala-play-framework-workshop

## CSRF (Cross-Site Request Forgery)

CSRF is a web security vulnerability where an attacker tricks a logged-in user’s browser into sending unwanted requests to a site where the user is already authenticated.  
**Example:** If you are logged into your bank and visit a malicious site, it could make your browser submit a hidden transfer request using your valid session cookie.

### Prevention
- **CSRF tokens**: The server generates a random token and embeds it in forms; only requests with the correct token are accepted.  
- **SameSite cookies**: Prevent cookies from being sent with cross-site requests.  
- **Play Framework**: Built-in CSRF protection automatically adds and validates tokens when you use `@helper.form` and `@CSRF.formField`.

---

## Session vs Flash in Play Framework

- **Session**
  - Type: `Map[String, String]` (stored in a cookie).
  - Persists across multiple requests until cleared or expired.
  - Usage: store long-lived user info like username, role, or language.
  - Cleared with `.withNewSession` or overwrite.

- **Flash**
  - Type: `Map[String, String]` (stored in a cookie).
  - Lives only for the **next request**, then automatically cleared.
  - Usage: short, one-time messages like “login success” or “form error”.

---

## What is `implicit request`?

### 1. The `request`
When a user visits your app (e.g., `GET /login`), Play creates a `Request` object that contains:
- Method (GET, POST, …)
- URL and query params
- Headers and cookies
- Session and Flash data

Think of it as the **envelope** carrying all request info.

### 2. The word `implicit`
In Scala, `implicit` means **“automatically supply this value when needed.”**  
So instead of passing `request` manually to every function, marking it `implicit` makes Play/Scala pass it for you.

### 3. Example
Without implicit:
```scala
def taskList = Action { request: RequestHeader =>
  Ok(views.html.taskList()(request)) // must pass request manually
}
```

## Explanation of `addTask` Action

The `addTask` action handles form submission, validates user and input, and updates tasks.  
Here are the key Scala/Play components it uses:

- **Action** (`play.api.mvc.Action`) → Defines a controller endpoint; must return a `Result`.  
- **implicit request** (`Request[AnyContent]`) → Provides the HTTP request automatically to code and views.  
- **request.body.asFormUrlEncoded** (`Option[Map[String, Seq[String]]]`) → Parses POST form fields; `Option` because body might not be form-encoded.  
- **getOrElse(Map.empty)** (`Map[String, Seq[String]]`) → Supplies a safe default empty map when the form is missing.  
- **form.get("task")** (`Option[Seq[String]]`) → Safely looks up the `"task"` field in the map.  
- **headOption** (`Option[String]`) → Picks the first element of a sequence without risking errors on empty input.  
- **map(_.trim)** (`Option[String]`) → Transforms the value inside `Some`, here trimming whitespace, while leaving `None` untouched.  
- **request.session.get("username")** (`Option[String]`) → Retrieves a value from the session cookie; safe because wrapped in `Option`.  
- **Pattern Matching** (`match`) → Handles all combinations (`Some`/`None`) in a clean, readable way, with guards like `if task.nonEmpty`.  
- **Redirect(...)** (`Result`) → Sends the browser to another route (e.g., the task list page).  
- **flashing("key" -> "value")** (`Result`) → Attaches a one-time success/error message to the next request.  
- **Unauthorized("...")** (`Result`) → Returns an HTTP 401 response when no user is found in session.  
- **Boolean return from addTask** → Model returns `true` or `false` to indicate success/failure; used for branching.


