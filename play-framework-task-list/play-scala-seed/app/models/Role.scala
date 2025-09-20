package models

sealed trait Role {
  def value: String
}

object Role {
  case object Admin extends Role {
    val value = "admin"
  }
  
  case object Basic extends Role {
    val value = "basic"
  }
  
  def fromString(role: String): Option[Role] = role.toLowerCase match {
    case "admin" => Some(Admin)
    case "basic" => Some(Basic)
    case _ => None
  }
  
  def toString(role: Role): String = role.value
}
