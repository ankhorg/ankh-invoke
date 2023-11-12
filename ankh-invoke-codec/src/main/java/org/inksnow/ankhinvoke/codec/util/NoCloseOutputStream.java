package org.inksnow.ankhinvoke.codec.util;

import org.jetbrains.annotations.NotNull;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class NoCloseOutputStream extends FilterOutputStream {
  public NoCloseOutputStream(@NotNull OutputStream out) {
    super(out);
  }

  @Override
  public void close() throws IOException {
    out.flush();
  }
}
