package utils

import org.scalatestplus.play.PlaySpec

class EmailValidatorSpec extends PlaySpec {

  "EmailValidator" should {
    
    "validate correct email addresses" in {
      val validEmails = Seq(
        "test@example.com",
        "user.name@domain.co.uk",
        "user+tag@example.org",
        "user123@test-domain.com",
        "a@b.co",
        "user@sub.domain.com"
      )
      
      validEmails.foreach { email =>
        EmailValidator.isValid(email) mustBe true
        EmailValidator.validateAndNormalize(email) mustBe Some(email.toLowerCase)
      }
    }
    
    "reject invalid email addresses" in {
      val invalidEmails = Seq(
        "",
        "invalid",
        "@example.com",
        "user@",
        "user@.com",
        "user..name@example.com",
        "user@example..com",
        "user name@example.com",
        "user@example com"
      )
      
      invalidEmails.foreach { email =>
        EmailValidator.isValid(email) mustBe false
        EmailValidator.validateAndNormalize(email) mustBe None
      }
    }
    
    "normalize email addresses correctly" in {
      EmailValidator.normalize("  Test@Example.COM  ") mustBe "test@example.com"
      EmailValidator.normalize("USER@DOMAIN.ORG") mustBe "user@domain.org"
      EmailValidator.normalize("") mustBe ""
      EmailValidator.normalize(null) mustBe ""
    }
    
    "handle edge cases" in {
      EmailValidator.isValid(null) mustBe false
      EmailValidator.validateAndNormalize(null) mustBe None
      EmailValidator.validateAndNormalize("   ") mustBe None
    }
  }
}
