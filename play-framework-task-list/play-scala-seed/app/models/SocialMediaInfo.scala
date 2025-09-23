package models

final case class SocialMediaInfo(
  id: Long = 0L,
  country: Country,
  title: String,
  content: String,
  isActive: Boolean = true,
  createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now()
)
