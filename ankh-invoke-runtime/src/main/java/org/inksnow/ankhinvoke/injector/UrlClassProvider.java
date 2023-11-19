package org.inksnow.ankhinvoke.injector;

import org.inksnow.ankhinvoke.comments.InternalName;
import org.inksnow.ankhinvoke.util.DstUnsafe;
import org.inksnow.ankhinvoke.util.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public interface UrlClassProvider extends ClassProvider {
  @Nullable URL provideUrl(@InternalName @NotNull String name);

  default byte @Nullable [] provide(@InternalName @NotNull String name) {
    URL url = provideUrl(name);
    if (url == null) {
      return null;
    }
    try(InputStream in = url.openStream()) {
      return IOUtils.readAllBytes(in);
    } catch (IOException e) {
      throw DstUnsafe.throwImpl(e);
    }
  }
}
