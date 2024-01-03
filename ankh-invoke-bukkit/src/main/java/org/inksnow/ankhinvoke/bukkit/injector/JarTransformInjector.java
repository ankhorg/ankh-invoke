package org.inksnow.ankhinvoke.bukkit.injector;

import bot.inker.acj.JvmHacker;
import org.inksnow.ankhinvoke.AnkhInvoke;
import org.inksnow.ankhinvoke.comments.InternalName;
import org.inksnow.ankhinvoke.comments.NormalName;
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
import java.util.*;
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
  private final @NotNull List<@InternalName @NotNull String> transformPackages;
  private @Nullable AnkhInvoke ankhInvoke;

  private final @NotNull Map<@NotNull String, byte @NotNull []> memoryRepository = new ConcurrentSkipListMap<>();
  private final @NotNull Set<@NotNull String> usedClasses = new ConcurrentSkipListSet<>();
  private final @NotNull Set<@NotNull String> threadHiddenClasses = new ConcurrentSkipListSet<>();

  private JarTransformInjector(@NotNull ClassLoader classLoader, @NotNull List<@InternalName @NotNull String> transformPackages) {
    this.classLoader = classLoader;
    this.transformPackages = transformPackages;
    if (!PLUGIN_CLASS_LOADER_CLASS.isInstance(classLoader)) {
      throw new IllegalArgumentException("JarTransformInjector classLoader must be instance of " + PLUGIN_CLASS_LOADER_CLASS_NAME);
    }
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
          .asType(MethodType.methodType(void.class, ClassLoader.class, JarFile.class));
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

  private boolean needTransform(@NotNull String className) {
    return transformPackages.stream()
        .anyMatch(className::startsWith);
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

  public static final class Builder {
    private final @NotNull Set<@InternalName @NotNull String> transformPackages = new LinkedHashSet<>();
    private @Nullable ClassLoader classLoader;

    public @NotNull Builder classLoader(@NotNull ClassLoader classLoader) {
      this.classLoader = classLoader;
      return this;
    }

    public @NotNull Builder transformPackage(@NormalName @NotNull String packageName) {
      this.transformPackages.add(packageName.replace('.', '/'));
      return this;
    }

    public @NotNull JarTransformInjector build() {
      if (classLoader == null) {
        throw new IllegalStateException("classLoader not set");
      }
      return new JarTransformInjector(classLoader, new ArrayList<>(transformPackages));
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
      if (ankhInvoke == null) {
        throw new IllegalStateException("jar-transform-injector not ready");
      }

      if (!name.endsWith(".class")) {
        return jarFile.getJarEntry(name);
      }

      String className = name.substring(0, name.length() - ".class".length());
      if (threadHiddenClasses.contains(className) || (
          !needTransform(className)
              && jarFile.getJarEntry(className + ".ankh-invoke.class") == null)) {
        return jarFile.getJarEntry(name);
      }

      try {
        threadHiddenClasses.add(className);
        provideBytes(className);
      } catch (FileNotFoundException e) {
        return null;
      } catch (IOException e) {
        throw DstUnsafe.throwImpl(e);
      } finally {
        threadHiddenClasses.remove(className);
      }
      return new JarEntry(name);
    }

    @Override
    public ZipEntry getEntry(String name) {
      return getJarEntry(name);
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
      if (!ze.getName().endsWith(".class")) {
        return jarFile.getInputStream(ze);
      }

      String className = ze.getName().substring(0, ze.getName().length() - ".class".length());

      byte[] bytes = memoryRepository.get(className);

      if (bytes == null) {
        return jarFile.getInputStream(ze);
      } else {
        return new ByteArrayInputStream(bytes);
      }
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
