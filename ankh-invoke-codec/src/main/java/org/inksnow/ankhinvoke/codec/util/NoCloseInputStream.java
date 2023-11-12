package org.inksnow.ankhinvoke.codec.util;

import org.jetbrains.annotations.NotNull;

import java.io.*;

public class NoCloseInputStream extends FilterInputStream {
  public NoCloseInputStream(@NotNull InputStream in) {
    super(in);
  }

  @Override
  public void close() throws IOException {
    //
  }
}
