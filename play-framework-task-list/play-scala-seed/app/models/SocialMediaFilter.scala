package models

sealed trait SocialMediaFilter {
  def name: String
  def description: String
  def apply(infos: Seq[SocialMediaInfo]): Seq[SocialMediaInfo]
}

object SocialMediaFilter {
  
  case object AllCountries extends SocialMediaFilter {
    val name = "all"
    val description = "All Countries"
    def apply(infos: Seq[SocialMediaInfo]): Seq[SocialMediaInfo] = infos
  }
  
  case class ByCountry(country: Country) extends SocialMediaFilter {
    val name = s"country-${country.value}"
    val description = s"${country.displayName} Only"
    def apply(infos: Seq[SocialMediaInfo]): Seq[SocialMediaInfo] = 
      infos.filter(_.country == country)
  }
  
  // Parse country filter from query parameters
  def fromQueryParams(params: Map[String, Seq[String]]): Option[SocialMediaFilter] = {
    params.get("country").flatMap(_.headOption).flatMap { countryCode =>
      Country.fromString(countryCode).map(ByCountry.apply)
    }
  }
  
}
