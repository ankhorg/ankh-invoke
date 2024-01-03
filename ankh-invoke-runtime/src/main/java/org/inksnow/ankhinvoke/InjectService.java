package org.inksnow.ankhinvoke;

import org.inksnow.ankhinvoke.comments.InternalName;
import org.inksnow.ankhinvoke.injector.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URLClassLoader;
import java.security.ProtectionDomain;

public final class InjectService {
  private final @NotNull ClassInjector injector;
  private final @NotNull ClassProvider provider;

  private InjectService(@NotNull ClassInjector injector, @NotNull ClassProvider provider) {
    this.injector = injector;
    this.provider = provider;
  }

  public @NotNull Class<?> inject(@InternalName @NotNull String name, byte @NotNull [] bytes, @Nullable ProtectionDomain protectionDomain) {
    return injector.inject(name, bytes, protectionDomain);
  }

  public byte @Nullable [] provide(@InternalName @NotNull String name) {
    return provider.provide(name);
  }

  public @NotNull ClassInjector getInjector() {
    return injector;
  }

  public @NotNull ClassProvider getProvider() {
    return provider;
  }

  public static final class Builder {
    private final AnkhInvoke.@NotNull Builder ankhInvokeBuilder;
    private @Nullable ClassInjector injector;
    private @Nullable ClassProvider provider;

    /* package-private */ Builder(AnkhInvoke.@NotNull Builder ankhInvokeBuilder) {
      this.ankhInvokeBuilder = ankhInvokeBuilder;
    }

    public @NotNull Builder injector(@NotNull ClassInjector injector) {
      this.injector = injector;
      return this;
    }

    public @NotNull Builder unsafeInjector(@NotNull ClassLoader classLoader) {
      return injector(new UnsafeClassInjector(classLoader));
    }

    public @NotNull Builder urlInjector(@NotNull URLClassLoader urlClassLoader) {
      return injector(new UrlTransformInjector(urlClassLoader));
    }

    public @NotNull Builder instrumentationInjector(@NotNull ClassLoader classLoader, @NotNull String @NotNull ... applyPrefixes) {
      return injector(new InstrumentationTransformInjector(classLoader, applyPrefixes));
    }

    public @NotNull Builder provider(@NotNull ClassProvider provider) {
      this.provider = provider;
      return this;
    }

    public @NotNull Builder classLoaderProvider(@NotNull ClassLoader classLoader) {
      return provider(new ClassLoaderProvider(classLoader));
    }

    public AnkhInvoke.@NotNull Builder build() {
      return ankhInvokeBuilder;
    }

    /* package-private */
    @NotNull InjectService buildInternal() {
      if (injector == null) {
        throw new IllegalArgumentException("must special a inject service");
      }
      if (provider == null) {
        throw new IllegalArgumentException("must special a provide service");
      }
      return new InjectService(injector, provider);
    }
  }
}
