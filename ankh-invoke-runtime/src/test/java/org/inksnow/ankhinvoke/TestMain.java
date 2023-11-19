package org.inksnow.ankhinvoke;

import org.inksnow.ankhinvoke.classpool.ClassLoaderPoolLoader;
import org.inksnow.ankhinvoke.classpool.LoadedClassPoolLoader;
import org.inksnow.ankhinvoke.classpool.ResourcePoolLoader;
import org.inksnow.ankhinvoke.reference.ResourceReferenceSource;
import org.slf4j.impl.SimpleLoggerConfiguration;

public class TestMain {
  static {
    ClassLoader pluginClassLoader = TestMain.class.getClassLoader();
    AnkhInvoke ankhInvoke = AnkhInvoke.builder()
        .reference()
        /**/.appendPackage("org.inksnow.ankhinvoke.ref")
        /**/.appendSource(new ResourceReferenceSource(pluginClassLoader))
        /**/.build()
        .inject()
        /**/.unsafeInjector(pluginClassLoader)
        /**/.classLoaderProvider(pluginClassLoader)
        /**/.build()
        .classPool()
        /**/.appendLoader(new LoadedClassPoolLoader(pluginClassLoader))
        /**/.appendLoader(new ResourcePoolLoader(pluginClassLoader))
        /**/.appendLoader(new ClassLoaderPoolLoader(pluginClassLoader))
        /**/.build()
        .build();
    ankhInvoke.get("org.inksnow.ankhinvoke.TheMain");
  }

  public static void main(String[] args) {
    TheMain.main(args);
  }
}
