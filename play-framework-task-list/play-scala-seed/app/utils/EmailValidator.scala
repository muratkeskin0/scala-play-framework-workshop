package utils

import scala.util.matching.Regex

object EmailValidator {
  
  // RFC 5322 uyumlu email regex pattern
  private val emailRegex: Regex = """^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r
  
  /**
   * Email adresinin geçerli olup olmadığını kontrol eder
   * @param email Kontrol edilecek email adresi
   * @return true eğer email geçerliyse, false değilse
   */
  def isValid(email: String): Boolean = {
    email != null && email.trim.nonEmpty && emailRegex.matches(email.trim)
  }
  
  /**
   * Email adresini normalize eder (trim ve lowercase)
   * @param email Normalize edilecek email
   * @return Normalize edilmiş email
   */
  def normalize(email: String): String = {
    if (email == null) ""
    else email.trim.toLowerCase
  }
  
  /**
   * Email validation ile birlikte normalize eder
   * @param email Kontrol edilecek ve normalize edilecek email
   * @return Some(normalizedEmail) eğer geçerliyse, None değilse
   */
  def validateAndNormalize(email: String): Option[String] = {
    val normalized = normalize(email)
    if (isValid(normalized)) Some(normalized) else None
  }
}
