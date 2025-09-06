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

# Key Scala / Play Concepts

- **Action** (`play.api.mvc.Action`)  
  Defines a controller endpoint in Play Framework.  
  It takes a block of code that produces a `Result` (`Ok`, `Redirect`, `Unauthorized`, etc.) in response to an HTTP request.

- **implicit request** (`Request[AnyContent]`)  
  An implicit parameter makes the `Request` object automatically available to code, views, and helpers without passing it manually.  
  Useful for accessing headers, session, flash, and CSRF tokens.

- **Option** (`Option[T]`)  
  A container for an optional value: either `Some(value)` or `None`.  
  Used to safely handle missing data instead of using `null`.

- **getOrElse** (`Option[T] → T`)  
  Provides a default value if an `Option` is `None`.  
  Example: `opt.getOrElse("default")`.

- **Map** (`Map[K, V]`)  
  A key-value collection.  
  - `form.get("task")` returns an `Option[Seq[String]]`.  
  - Access is safe when combined with `Option` methods.

- **headOption** (`Seq[T] → Option[T]`)  
  Returns the first element of a sequence wrapped in `Some`, or `None` if the sequence is empty.  
  Safer than `head`, which throws an exception on empty lists.

- **map** (on `Option`, `Seq`, etc.)  
  Applies a function to the contents if present.  
  Example: `opt.map(_.trim)` modifies the value inside `Some` but leaves `None` unchanged.

- **Pattern Matching** (`match` expression)  
  A powerful construct to branch logic by matching values against patterns.  
  Works well with `Option` (`Some` / `None`) and includes guards like `if condition`.

- **Redirect(...)** (`Result`)  
  Returns an HTTP redirect response to a different route or URL.  
  Commonly used after form submissions.

- **Flash** (`Flash`, backed by `Map[String, String]`)  
  A short-lived message scope, surviving for exactly one request.  
  `.flashing("key" -> "msg")` attaches a one-time message (e.g., success/error).

- **Unauthorized("...")** (`Result`)  
  A convenience method to return HTTP 401 Unauthorized.  
  Indicates the request cannot proceed without valid authentication.

- **Boolean return values**  
  `true` or `false` are often used in models/services to signal success/failure of an operation.  
  In functional Scala, returning richer types like `Either` or `Try` is often preferred for more detailed error handling.

## Scala `patch` Method

The `patch` method creates a new sequence by replacing or removing elements at a given position.

**Signature**:
```scala
def patch(from: Int, other: Seq[A], replaced: Int): Seq[A]
```


# Play Framework Forms - Quick Guide

A concise guide to working with forms in Scala Play Framework.

## Basic Form Setup

**1. Define case class:**
```scala
case class UserData(username: String, email: String, age: Int)
```

**2. Create form mapping:**
```scala
import play.api.data._
import play.api.data.Forms._

val userForm: Form[UserData] = Form(
  mapping(
    "username" -> nonEmptyText(minLength = 3),
    "email" -> email,
    "age" -> number(min = 18)
  )(UserData.apply)(UserData.unapply)
)
```

## Controller Actions

**GET - Show form:**
```scala
def showForm = Action { implicit request =>
  Ok(views.html.userForm(userForm))
}
```

**POST - Process form:**
```scala
def submitForm = Action { implicit request =>
  userForm.bindFromRequest().fold(
    formWithErrors => BadRequest(views.html.userForm(formWithErrors)),
    userData => Redirect("/success").flashing("info" -> "User created!")
  )
}
```

## Twirl Template

```scala
@(form: Form[UserData])(implicit request: MessagesRequestHeader)
@import helper._

@helper.form(action = routes.UserController.submitForm()) {
  @CSRF.formField
  
  @helper.inputText(form("username"))
  @helper.email(form("email"))
  @helper.inputText(form("age"))
  
  <button type="submit">Submit</button>
}
```

## Key Features

- **bindFromRequest()** - Extracts and validates form data
- **fold()** - Handles success/error cases
- **formWithErrors** - Preserves user input + shows validation errors
- **@CSRF.formField** - CSRF protection
- **Flash messages** - One-time notifications
- **Session** - Persistent user data

## Common Validators

- `nonEmptyText` - Required text field
- `email` - Valid email format
- `number(min, max)` - Numeric range
- `boolean` - Checkbox
- `text(minLength, maxLength)` - Text length constraints

## Routes

```scala
GET  /users/new     controllers.UserController.showForm
POST /users         controllers.UserController.submitForm
```

That's it! Forms provide type-safe request handling with built-in validation and error management.

