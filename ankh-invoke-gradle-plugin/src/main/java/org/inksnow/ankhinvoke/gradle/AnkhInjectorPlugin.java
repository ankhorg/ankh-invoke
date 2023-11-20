package org.inksnow.ankhinvoke.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

public class AnkhInjectorPlugin implements Plugin<Project>  {
  public static final @NotNull String ANKH_INVOKE_PACKAGE = "org.inksnow.ankhinvoke";

  @Override
  public void apply(@NotNull Project target) {
    //
  }
}
