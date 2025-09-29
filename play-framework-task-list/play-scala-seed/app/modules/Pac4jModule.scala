package modules

import com.google.inject.{AbstractModule, Provides, Singleton}
import play.cache.SyncCacheApi
import org.pac4j.play.store.{PlaySessionStore, PlayCacheSessionStore}
import org.pac4j.core.config.Config
import javax.inject._

class Pac4jModule extends AbstractModule {
  override def configure(): Unit = {}

  @Provides @Singleton
  def providePlaySessionStore(cache: SyncCacheApi): PlaySessionStore =
    new PlayCacheSessionStore(cache)

  @Provides @Singleton
  def provideConfig(securityModule: security.SecurityModule): Config =
    securityModule.getPac4jConfig
}

