package org.inksnow.ankhinvoke.bukkit;

import org.bukkit.plugin.java.JavaPlugin;
import org.inksnow.ankhinvoke.AnkhInvoke;
import org.inksnow.ankhinvoke.bukkit.asm.NmsVersionRemapper;
import org.inksnow.ankhinvoke.classpool.ClassLoaderPoolLoader;
import org.inksnow.ankhinvoke.classpool.LoadedClassPoolLoader;
import org.inksnow.ankhinvoke.classpool.ResourcePoolLoader;
import org.inksnow.ankhinvoke.reference.ResourceReferenceSource;
import org.jetbrains.annotations.NotNull;

public class AnkhInvokeBukkit {
  private AnkhInvokeBukkit() {
    throw new UnsupportedOperationException();
  }

  public static AnkhInvoke.@NotNull Builder forBukkit(@NotNull Class<? extends JavaPlugin> pluginClass) {
    ClassLoader pluginClassLoader = pluginClass.getClassLoader();
    return AnkhInvoke.builder()
        .reference()
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
        .referenceRemap()
        /**/.append(new NmsVersionRemapper())
        /**/.build();
  }
}
