package models


sealed trait Country {
  def value: String
  def displayName: String
}

object Country {
  case object Turkey extends Country {
    val value = "tr"
    val displayName = "Türkiye"
  }
  
  case object UnitedStates extends Country {
    val value = "us"
    val displayName = "United States"
  }
  
  case object UnitedKingdom extends Country {
    val value = "uk"
    val displayName = "United Kingdom"
  }
  
  case object Germany extends Country {
    val value = "de"
    val displayName = "Germany"
  }
  
  case object France extends Country {
    val value = "fr"
    val displayName = "France"
  }
  
  case object Italy extends Country {
    val value = "it"
    val displayName = "Italy"
  }
  
  case object Spain extends Country {
    val value = "es"
    val displayName = "Spain"
  }
  
  case object Canada extends Country {
    val value = "ca"
    val displayName = "Canada"
  }
  
  case object Australia extends Country {
    val value = "au"
    val displayName = "Australia"
  }
  
  case object Japan extends Country {
    val value = "jp"
    val displayName = "Japan"
  }
  
  case object China extends Country {
    val value = "cn"
    val displayName = "China"
  }
  
  case object India extends Country {
    val value = "in"
    val displayName = "India"
  }
  
  case object Brazil extends Country {
    val value = "br"
    val displayName = "Brazil"
  }
  
  case object Russia extends Country {
    val value = "ru"
    val displayName = "Russia"
  }
  
  case object Mexico extends Country {
    val value = "mx"
    val displayName = "Mexico"
  }
  
  case object Netherlands extends Country {
    val value = "nl"
    val displayName = "Netherlands"
  }
  
  case object Sweden extends Country {
    val value = "se"
    val displayName = "Sweden"
  }
  
  case object Norway extends Country {
    val value = "no"
    val displayName = "Norway"
  }
  
  case object Denmark extends Country {
    val value = "dk"
    val displayName = "Denmark"
  }
  
  case object Finland extends Country {
    val value = "fi"
    val displayName = "Finland"
  }
  
  case object Other extends Country {
    val value = "other"
    val displayName = "Other"
  }
  
  val allCountries: List[Country] = List(
    Turkey, UnitedStates, UnitedKingdom, Germany, France, Italy, Spain, Canada,
    Australia, Japan, China, India, Brazil, Russia, Mexico, Netherlands,
    Sweden, Norway, Denmark, Finland, Other
  )
  
  def fromString(country: String): Option[Country] = country.toLowerCase match {
    case "tr" | "turkey" | "türkiye" => Some(Turkey)
    case "us" | "usa" | "united states" => Some(UnitedStates)
    case "uk" | "united kingdom" | "britain" => Some(UnitedKingdom)
    case "de" | "germany" | "deutschland" => Some(Germany)
    case "fr" | "france" => Some(France)
    case "it" | "italy" | "italia" => Some(Italy)
    case "es" | "spain" | "españa" => Some(Spain)
    case "ca" | "canada" => Some(Canada)
    case "au" | "australia" => Some(Australia)
    case "jp" | "japan" | "日本" => Some(Japan)
    case "cn" | "china" | "中国" => Some(China)
    case "in" | "india" | "भारत" => Some(India)
    case "br" | "brazil" | "brasil" => Some(Brazil)
    case "ru" | "russia" | "россия" => Some(Russia)
    case "mx" | "mexico" | "méxico" => Some(Mexico)
    case "nl" | "netherlands" | "holland" => Some(Netherlands)
    case "se" | "sweden" | "sverige" => Some(Sweden)
    case "no" | "norway" | "norge" => Some(Norway)
    case "dk" | "denmark" | "danmark" => Some(Denmark)
    case "fi" | "finland" | "suomi" => Some(Finland)
    case "other" => Some(Other)
    case _ => None
  }
  
  def toString(country: Country): String = country.value
}
