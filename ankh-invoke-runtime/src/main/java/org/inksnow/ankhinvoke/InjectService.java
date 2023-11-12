package org.inksnow.ankhinvoke;

import org.inksnow.ankhinvoke.comments.InternalName;
import org.inksnow.ankhinvoke.injector.ClassInjector;
import org.inksnow.ankhinvoke.injector.ClassLoaderProvider;
import org.inksnow.ankhinvoke.injector.ClassProvider;
import org.inksnow.ankhinvoke.injector.UnsafeClassInjector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

  public static final class Builder {
    private final AnkhInvoke.@NotNull Builder ankhInvokeBuilder;
    private @Nullable ClassInjector injector;
    private @Nullable ClassProvider provider;

    /* package-private */ Builder(AnkhInvoke.@NotNull Builder ankhInvokeBuilder) {
      this.ankhInvokeBuilder = ankhInvokeBuilder;
    }

    public @NotNull Builder unsafeInjector(@NotNull ClassLoader classLoader) {
      this.injector = new UnsafeClassInjector(classLoader);
      return this;
    }

    public @NotNull Builder classLoaderProvider(@NotNull ClassLoader classLoader) {
      this.provider = new ClassLoaderProvider(classLoader);
      return this;
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
