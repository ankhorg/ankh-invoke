package org.inksnow.ankhinvoke.injector;

import bot.inker.acj.JvmHacker;
import org.inksnow.ankhinvoke.AnkhInvoke;
import org.inksnow.ankhinvoke.comments.InternalName;
import org.inksnow.ankhinvoke.util.DstUnsafe;
import org.inksnow.ankhinvoke.util.SpyHandle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.net.*;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class UrlTransformInjector extends URLStreamHandler implements TransformInjector {
  private static final @NotNull Logger logger = LoggerFactory.getLogger(UrlTransformInjector.class);
  private static final @NotNull MethodHandle URL_CLASS_LOADER_ADD_URL_HANDLE = createUrlClassLoaderAddUrlHandle();
  public static final @NotNull String PROTOCOL_PREFIX = "ankh-invoke-internal-url-transform-injector-";

  private final @NotNull String protocol;
  private final @NotNull URLClassLoader urlClassLoader;
  private @Nullable AnkhInvoke ankhInvoke;

  private final @NotNull Map<@NotNull String, byte @NotNull []> memoryRepository = new ConcurrentSkipListMap<>();
  private final @NotNull Set<@NotNull String> usedClasses = new ConcurrentSkipListSet<>();
  private final @NotNull Set<@NotNull String> threadHiddenClasses = new ConcurrentSkipListSet<>();

  public UrlTransformInjector(@NotNull URLClassLoader urlClassLoader) {
    this.protocol = PROTOCOL_PREFIX + System.identityHashCode(this);
    this.urlClassLoader = urlClassLoader;
  }

  private static @NotNull MethodHandle createUrlClassLoaderAddUrlHandle() {
    try {
      return JvmHacker.lookup().findVirtual(URLClassLoader.class, "addURL", MethodType.methodType(void.class, URL.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw DstUnsafe.throwImpl(e);
    }
  }

  private static void urlClassLoaderAddUrl(@NotNull URLClassLoader urlClassLoader, @NotNull URL url) {
    try {
      URL_CLASS_LOADER_ADD_URL_HANDLE.invokeExact(urlClassLoader, url);
    } catch (Throwable e) {
      throw DstUnsafe.throwImpl(e);
    }
  }

  @Override
  public @NotNull Class<?> inject(@InternalName @NotNull String name, byte @NotNull [] bytes, @Nullable ProtectionDomain protectionDomain) {
    memoryRepository.put(name, bytes);
    Class<?> clazz;
    try {
      clazz = Class.forName(name.replace('/', '.'), false, urlClassLoader);
    } catch (ClassNotFoundException e) {
      throw DstUnsafe.throwImpl(e);
    }
    if (!usedClasses.contains(name)) {
      String msg = "Failed to inject " + name +" with url, did you apply ankh-invoke-userdev?";
      logger.error(msg, new IllegalStateException(msg));
    }
    return clazz;
  }

  @Override
  public void registerHandle(@NotNull AnkhInvoke ankhInvoke) {
    this.ankhInvoke = ankhInvoke;
    SpyHandle.injectSpy(urlClassLoader);
    try {
      urlClassLoaderAddUrl(urlClassLoader, new URL(this.protocol, null, -1, "/", this));
    } catch (MalformedURLException e) {
      throw DstUnsafe.throwImpl(e);
    }
  }

  @Override
  protected @NotNull URLConnection openConnection(@NotNull URL u) throws IOException {
    if(ankhInvoke == null) {
      throw new IOException("url-transform-injector not ready");
    }
    if (!protocol.equals(u.getProtocol())) {
      throw new ProtocolException("unsupported protocol: " + protocol);
    }

    String className = fetchClassNameFromUrl(u);

    if (threadHiddenClasses.contains(className)) {
      throw new FileNotFoundException("class: " + className + " is pending loading source");
    }

    ClassProvider classProvider = ankhInvoke.injectService().getProvider();
    if (classProvider instanceof UrlClassProvider) {
      URL url;
      try {
        threadHiddenClasses.add(className);
        url = ((UrlClassProvider) classProvider).provideUrl(className);
      }finally {
        threadHiddenClasses.remove(className);
      }
      if (url == null) {
        throw new FileNotFoundException("class: " + className);
      }
      URLConnection connection = url.openConnection();

      if (connection instanceof JarURLConnection) {
        return new JarTransformUrlConnection(u, (JarURLConnection) connection, provideBytes(className));
      } else {
        return new UrlTransformUrlConnection(u, provideBytes(className));
      }
    } else {
      return new UrlTransformUrlConnection(u, provideBytes(className));
    }
  }

  private @NotNull String fetchClassNameFromUrl(@NotNull URL u) throws IOException {
    String fileName = u.getFile();
    if (fileName.startsWith("/")) {
      fileName = fileName.substring(1);
    }
    if (!fileName.endsWith(".class")) {
      throw new FileNotFoundException(fileName);
    }
    String className = fileName.substring(0, fileName.length() - ".class".length());

    if (className.endsWith(".ankh-invoke")) {
      throw new FileNotFoundException("class: " + className + " is loaded by provider only");
    }
    return className;
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

  private static final class UrlTransformUrlConnection extends URLConnection {
    private final byte @NotNull [] bytes;
    private @Nullable ByteArrayInputStream byteArrayInputStream;

    private UrlTransformUrlConnection(@NotNull URL url, byte @NotNull [] bytes) {
      super(url);
      this.bytes = bytes;
    }

    @Override
    public void connect() {
      this.byteArrayInputStream = new ByteArrayInputStream(this.bytes);
    }

    @Override
    public long getContentLengthLong() {
      return bytes.length;
    }

    @Override
    public @NotNull InputStream getInputStream() {
      if (this.byteArrayInputStream == null) {
        this.connect();
      }
      return this.byteArrayInputStream;
    }
  }

  private static final class JarTransformUrlConnection extends JarURLConnection {
    private final @NotNull URL url;
    private final @NotNull JarURLConnection handleByConnection;
    private final byte @NotNull [] bytes;
    private @Nullable ByteArrayInputStream byteArrayInputStream;

    private JarTransformUrlConnection(@NotNull URL url, @NotNull JarURLConnection handleByConnection, byte @NotNull [] bytes) throws MalformedURLException {
      super(handleByConnection.getURL());
      this.url = url;
      this.handleByConnection = handleByConnection;
      this.bytes = bytes;
    }

    @Override
    public @NotNull URL getURL() {
      return url;
    }

    @Override
    public @NotNull URL getJarFileURL() {
      return handleByConnection.getJarFileURL();
    }

    @Override
    public @NotNull String getEntryName() {
      return handleByConnection.getEntryName();
    }

    @Override
    public @NotNull JarFile getJarFile() throws IOException {
      return handleByConnection.getJarFile();
    }

    @Override
    public @NotNull Manifest getManifest() throws IOException {
      return handleByConnection.getManifest();
    }

    @Override
    public @Nullable JarEntry getJarEntry() throws IOException {
      return handleByConnection.getJarEntry();
    }

    @Override
    public @Nullable Attributes getAttributes() throws IOException {
      return handleByConnection.getAttributes();
    }

    @Override
    public @Nullable Attributes getMainAttributes() throws IOException {
      return handleByConnection.getMainAttributes();
    }

    @Override
    public @NotNull Certificate @Nullable [] getCertificates() throws IOException {
      return handleByConnection.getCertificates();
    }

    @Override
    public @Range(from = 0, to = Integer.MAX_VALUE) long getContentLengthLong() {
      return bytes.length;
    }

    @Override
    public void connect() {
      this.byteArrayInputStream = new ByteArrayInputStream(this.bytes);
    }

    @Override
    public @NotNull InputStream getInputStream() {
      if (this.byteArrayInputStream == null) {
        this.connect();
      }
      return this.byteArrayInputStream;
    }
  }
}
