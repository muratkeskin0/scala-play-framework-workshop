package forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._

object UserForms {

  case class LoginData(email: String, password: String)
  case class SignUpData(email: String, password: String, confirmPassword: String)

  val loginForm: Form[LoginData] = Form(
    mapping(
      "email" -> email.verifying(nonEmpty, maxLength(255)),
      "password" -> nonEmptyText(minLength = 6, maxLength = 100)
    )(LoginData.apply)(LoginData.unapply)
  )

  val signUpForm: Form[SignUpData] = Form(
    mapping(
      "email" -> email.verifying(nonEmpty, maxLength(255)),
      "password" -> nonEmptyText(minLength = 6, maxLength = 100),
      "confirmPassword" -> nonEmptyText(minLength = 6, maxLength = 100)
    )(SignUpData.apply)(SignUpData.unapply)
      .verifying("Passwords do not match", data => data.password == data.confirmPassword)
  )

  val profileUpdateForm: Form[LoginData] = Form(
    mapping(
      "email" -> email.verifying(nonEmpty, maxLength(255)),
      "password" -> nonEmptyText(minLength = 6, maxLength = 100)
    )(LoginData.apply)(LoginData.unapply)
  )
}
