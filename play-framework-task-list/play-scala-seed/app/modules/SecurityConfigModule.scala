package modules

import com.google.inject.{AbstractModule, Provides, Singleton}
import org.pac4j.core.config.Config
import org.pac4j.play.store.PlaySessionStore
import org.pac4j.play.{PlayWebContext}
import play.api.{Configuration, Environment}
import play.cache.SyncCacheApi
import security.SecurityModule

class SecurityConfigModule(environment: Environment, configuration: Configuration) extends AbstractModule {

  override def configure(): Unit = {
    // SecurityModule'u singleton olarak bind et  
    bind(classOf[SecurityModule]).asEagerSingleton()
  }

  @Provides
  @Singleton
  def providePlaySessionStore(syncCacheApi: SyncCacheApi): PlaySessionStore = {
    // PlaySessionStore'u doğru şekilde konfigüre et
    new org.pac4j.play.store.PlayCacheSessionStore(syncCacheApi)
  }

  @Provides
  @Singleton  
  def providePac4jConfig(securityModule: SecurityModule): Config = {
    println("🔧 SecurityConfigModule: Providing Pac4j Config")
    securityModule.getPac4jConfig
  }
}
