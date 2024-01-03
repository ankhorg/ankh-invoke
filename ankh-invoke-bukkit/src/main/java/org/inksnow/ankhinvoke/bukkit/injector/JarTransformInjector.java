package org.inksnow.ankhinvoke.bukkit.injector;

import bot.inker.acj.JvmHacker;
import org.inksnow.ankhinvoke.AnkhInvoke;
import org.inksnow.ankhinvoke.comments.InternalName;
import org.inksnow.ankhinvoke.injector.TransformInjector;
import org.inksnow.ankhinvoke.util.DstUnsafe;
import org.inksnow.ankhinvoke.util.SpyHandle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

public class JarTransformInjector implements TransformInjector {
  private static final @NotNull Logger logger = LoggerFactory.getLogger(JarTransformInjector.class);
  public static final @NotNull String PLUGIN_CLASS_LOADER_CLASS_NAME = "org.bukkit.plugin.java.PluginClassLoader";
  private static final @NotNull Class<?> PLUGIN_CLASS_LOADER_CLASS = createPluginClassLoaderClass();
  private static final @NotNull MethodHandle PLUGIN_CLASS_LOADER_GET_JAR_HANDLE = createPluginClassLoaderGetJarHandle();
  private static final @NotNull MethodHandle PLUGIN_CLASS_LOADER_SET_JAR_HANDLE = createPluginClassLoaderSetJarHandle();


  private final @NotNull ClassLoader classLoader;
  private @Nullable AnkhInvoke ankhInvoke;

  private final @NotNull Map<@NotNull String, byte @NotNull []> memoryRepository = new ConcurrentSkipListMap<>();
  private final @NotNull Set<@NotNull String> usedClasses = new ConcurrentSkipListSet<>();
  private final @NotNull Set<@NotNull String> threadHiddenClasses = new ConcurrentSkipListSet<>();

  public JarTransformInjector(@NotNull ClassLoader classLoader) {
    if (!PLUGIN_CLASS_LOADER_CLASS.isInstance(classLoader)) {
      throw new IllegalArgumentException("JarTransformInjector classLoader must be instance of " + PLUGIN_CLASS_LOADER_CLASS_NAME);
    }
    this.classLoader = classLoader;
  }

  private static @NotNull Class<?> createPluginClassLoaderClass() {
    try {
      return Class.forName(PLUGIN_CLASS_LOADER_CLASS_NAME);
    } catch (ClassNotFoundException e) {
      throw DstUnsafe.throwImpl(e);
    }
  }

  private static @NotNull MethodHandle createPluginClassLoaderGetJarHandle() {
    try {
      return JvmHacker.lookup()
          .findGetter(PLUGIN_CLASS_LOADER_CLASS, "jar", JarFile.class)
          .asType(MethodType.methodType(JarFile.class, ClassLoader.class));
    } catch (NoSuchFieldException e) {
      throw new IllegalStateException("No jar field found in " + PLUGIN_CLASS_LOADER_CLASS + ", not supported bukkit version");
    } catch (IllegalAccessException e) {
      throw DstUnsafe.throwImpl(e);
    }
  }

  private static @NotNull MethodHandle createPluginClassLoaderSetJarHandle() {
    try {
      return JvmHacker.lookup()
          .findSetter(PLUGIN_CLASS_LOADER_CLASS, "jar", JarFile.class)
          .asType(MethodType.methodType(void.class, JarFile.class, ClassLoader.class));
    } catch (NoSuchFieldException e) {
      throw new IllegalStateException("No jar field found in " + PLUGIN_CLASS_LOADER_CLASS + ", not supported bukkit version");
    } catch (IllegalAccessException e) {
      throw DstUnsafe.throwImpl(e);
    }
  }

  private static @NotNull JarFile pluginClassLoaderGetJar(@NotNull ClassLoader classLoader) {
    try {
      return (JarFile) PLUGIN_CLASS_LOADER_GET_JAR_HANDLE.invokeExact(classLoader);
    } catch (Throwable e) {
      throw DstUnsafe.throwImpl(e);
    }
  }

  private static void pluginClassLoaderSetJar(@NotNull ClassLoader classLoader, @NotNull JarFile jarFile) {
    try {
      PLUGIN_CLASS_LOADER_SET_JAR_HANDLE.invokeExact(classLoader, jarFile);
    } catch (Throwable e) {
      throw DstUnsafe.throwImpl(e);
    }
  }

  @Override
  public @NotNull Class<?> inject(@InternalName @NotNull String name, byte @NotNull [] bytes, @Nullable ProtectionDomain protectionDomain) {
    memoryRepository.put(name, bytes);
    Class<?> clazz;
    try {
      clazz = Class.forName(name.replace('/', '.'), false, classLoader);
    } catch (ClassNotFoundException e) {
      throw DstUnsafe.throwImpl(e);
    }
    if (AnkhInvoke.DEBUG && !usedClasses.contains(name)) {
      String msg = "Failed to inject " + name +" with url, loaded sub class but not load parent class?";
      logger.error(msg, new IllegalStateException(msg));
    }
    return clazz;
  }

  @Override
  public void registerHandle(@NotNull AnkhInvoke ankhInvoke) {
    this.ankhInvoke = ankhInvoke;
    SpyHandle.injectSpy(classLoader);
    JarFile jarFile = pluginClassLoaderGetJar(classLoader);
    if (jarFile == null) {
      throw new IllegalStateException("JarTransformInjector classLoader jarFile is null");
    }
    try {
      pluginClassLoaderSetJar(classLoader, new DelegateJarFile(new File(jarFile.getName()), jarFile));
    } catch (IOException e) {
      DstUnsafe.throwImpl(e);
    }
  }

  private byte @NotNull [] provideBytes(@InternalName @NotNull String className) throws IOException {
    if(ankhInvoke == null) {
      throw new IOException("url-transform-injector not ready");
    }
    try {
      threadHiddenClasses.add(className);
      byte[] bytes = memoryRepository.get(className);
      if (bytes == null) {
        bytes = ankhInvoke.processClass(className);
        if (bytes == null) {
          throw new FileNotFoundException("class: " + className);
        }
        memoryRepository.put(className, bytes);
      } else {
        usedClasses.add(className);
      }
      return bytes;
    }finally {
      threadHiddenClasses.remove(className);
    }
  }

  private final class DelegateJarFile extends JarFile {
    private final JarFile jarFile;

    public DelegateJarFile(File file, JarFile jarFile) throws IOException {
      super(file);
      this.jarFile = jarFile;
    }

    @Override
    public Manifest getManifest() throws IOException {
      return jarFile.getManifest();
    }

    @Override
    public JarEntry getJarEntry(String name) {
      return jarFile.getJarEntry(name);
    }

    @Override
    public ZipEntry getEntry(String name) {
      return jarFile.getEntry(name);
    }

    @Override
    public Enumeration<JarEntry> entries() {
      return jarFile.entries();
    }

    @Override
    public Stream<JarEntry> stream() {
      return jarFile.stream();
    }

    @Override
    public InputStream getInputStream(ZipEntry ze) throws IOException {
      if(ankhInvoke == null) {
        throw new IOException("jar-transform-injector not ready");
      }

      if(ze.getName().endsWith(".class")) {
        return jarFile.getInputStream(ze);
      }

      String className = ze.getName().substring(0, ze.getName().length() - ".class".length());

      if (threadHiddenClasses.contains(className)) {
        return jarFile.getInputStream(ze);
      }

      byte[] bytes;
      try {
        threadHiddenClasses.add(className);
        bytes = provideBytes(className);
      }finally {
        threadHiddenClasses.remove(className);
      }
      return new ByteArrayInputStream(bytes);
    }

    @Override
    public String getComment() {
      return jarFile.getComment();
    }

    @Override
    public String getName() {
      return jarFile.getName();
    }

    @Override
    public int size() {
      return jarFile.size();
    }

    @Override
    public void close() throws IOException {
      super.close();
      jarFile.close();
    }
  }
}
