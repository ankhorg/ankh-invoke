package org.inksnow.ankhinvoke;

import org.inksnow.ankhinvoke.classpool.ClassLoaderPoolLoader;
import org.inksnow.ankhinvoke.classpool.LoadedClassPoolLoader;
import org.inksnow.ankhinvoke.classpool.ResourcePoolLoader;
import org.inksnow.ankhinvoke.reference.ResourceReferenceSource;

import java.net.URLClassLoader;

public class TestMain {
  static {
    URLClassLoader urlClassLoader = (URLClassLoader) TestMain.class.getClassLoader();
    AnkhInvoke ankhInvoke = AnkhInvoke.builder()
        .reference()
        /**/.appendPackage("org.inksnow.ankhinvoke.ref")
        /**/.appendSource(new ResourceReferenceSource(urlClassLoader))
        /**/.build()
        .inject()
        /**/.urlInjector(urlClassLoader)
        /**/.classLoaderProvider(urlClassLoader)
        /**/.build()
        .classPool()
        /**/.appendLoader(new LoadedClassPoolLoader(urlClassLoader))
        /**/.appendLoader(new ResourcePoolLoader(urlClassLoader))
        /**/.appendLoader(new ClassLoaderPoolLoader(urlClassLoader))
        /**/.build()
        .build();
    ankhInvoke.get("org.inksnow.ankhinvoke.TheMain");
  }

  public static void main(String[] args) {
    TheMain.main(args);
  }
}
