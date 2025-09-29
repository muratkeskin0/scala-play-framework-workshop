package models

sealed trait LawFilter {
  def name: String
  def description: String
  def apply(infos: Seq[LawInfo]): Seq[LawInfo]
}

object LawFilter {
  
  case object AllCountries extends LawFilter {
    val name = "all"
    val description = "All Countries"
    def apply(infos: Seq[LawInfo]): Seq[LawInfo] = infos
  }
  
  case class ByCountry(country: Country) extends LawFilter {
    val name = s"country-${country.value}"
    val description = s"${country.displayName} Only"
    def apply(infos: Seq[LawInfo]): Seq[LawInfo] = 
      infos.filter(_.country == country)
  }
  
  // Parse country filter from query parameters
  def fromQueryParams(params: Map[String, Seq[String]]): Option[LawFilter] = {
    params.get("country").flatMap(_.headOption).flatMap { countryCode =>
      Country.fromString(countryCode).map(ByCountry.apply)
    }
  }
  
}
