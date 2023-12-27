package org.inksnow.ankhinvoke.map;

import org.jetbrains.annotations.NotNull;

public final class AnkhInvokeMapping {
  public static final @NotNull String ANKH_INVOKE_PACKAGE = new String("org.inksnow.ankhinvoke");

  public static final boolean DEBUG = Boolean.parseBoolean(System.getProperty(ANKH_INVOKE_PACKAGE + ".debug"));

  private AnkhInvokeMapping() {
    throw new UnsupportedOperationException();
  }
}
