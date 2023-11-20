package org.inksnow.ankhinvoke;

import org.inksnow.ankhinvoke.classpool.ClassLoaderPoolLoader;
import org.inksnow.ankhinvoke.classpool.LoadedClassPoolLoader;
import org.inksnow.ankhinvoke.classpool.ResourcePoolLoader;
import org.inksnow.ankhinvoke.reference.ResourceReferenceSource;
import org.inksnow.ankhinvoke.test.TheMain;

public class TestMain {
  static {
    ClassLoader classLoader = TestMain.class.getClassLoader();
    AnkhInvoke ankhInvoke = AnkhInvoke.builder()
        .reference()
        /**/.appendPackage("org.inksnow.ankhinvoke.ref")
        /**/.appendSource(new ResourceReferenceSource(classLoader))
        /**/.build()
        .inject()
        /**/.instrumentationInjector(classLoader, "org.inksnow.ankhinvoke.test")
        /**/.classLoaderProvider(classLoader)
        /**/.build()
        .classPool()
        /**/.appendLoader(new LoadedClassPoolLoader(classLoader))
        /**/.appendLoader(new ResourcePoolLoader(classLoader))
        /**/.appendLoader(new ClassLoaderPoolLoader(classLoader))
        /**/.build()
        .build();
  }

  public static void main(String[] args) {
    TheMain.main(args);
  }
}
