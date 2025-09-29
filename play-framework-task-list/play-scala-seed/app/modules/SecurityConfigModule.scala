package modules

import com.google.inject.AbstractModule
import security.SecurityModule

class SecurityConfigModule extends AbstractModule {

  override def configure(): Unit = {
    // SecurityModule'u singleton olarak bind et  
    bind(classOf[SecurityModule]).asEagerSingleton()
  }
}
