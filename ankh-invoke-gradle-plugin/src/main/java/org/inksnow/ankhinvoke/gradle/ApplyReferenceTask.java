package org.inksnow.ankhinvoke.gradle;

import com.google.common.io.Files;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.inksnow.ankhinvoke.map.BlobReferenceGenerator;
import org.inksnow.ankhinvoke.map.JarReferenceGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApplyReferenceTask extends DefaultTask {
  private @NotNull String ankhInvokePackage = AnkhInjectorPlugin.ANKH_INVOKE_PACKAGE;
  private final @NotNull List<@NotNull String> referencePackages = new ArrayList<>();
  private boolean disableClassRename;

  private @Nullable FileCollection inputJars;
  private @Nullable File outputJar;

  @Input
  public @NotNull String getAnkhInvokePackage() {
    return ankhInvokePackage;
  }

  public void setAnkhInvokePackage(@NotNull String ankhInvokePackage) {
    this.ankhInvokePackage = ankhInvokePackage;
  }

  @Input
  public @NotNull List<@NotNull String> getReferencePackages() {
    return Collections.unmodifiableList(referencePackages);
  }

  public void appendReferencePackage(@NotNull String packageName) {
    referencePackages.add(packageName);
  }

  @Input
  public boolean isDisableClassRename() {
    return disableClassRename;
  }

  public void setDisableClassRename(boolean disableClassRename) {
    this.disableClassRename = disableClassRename;
  }

  @InputFiles
  public @NotNull FileCollection getInputJars() {
    if (inputJars == null) {
      throw new IllegalStateException("missing require property 'inputJars'");
    }
    return inputJars;
  }

  public void setInputJars(@NotNull FileCollection inputJars) {
    this.inputJars = inputJars;
  }

  @OutputFile
  public @NotNull File getOutputJar() {
    if (outputJar == null) {
      throw new IllegalStateException("missing require property 'outputJar'");
    }
    return outputJar;
  }

  public void setOutputJar(@NotNull File outputJar) {
    this.outputJar = outputJar;
  }

  private void execute() throws Exception {
    File cacheDirectory = new File(getProject().getBuildDir(), "ankh-invoke-cache");
    JarReferenceGenerator generator = new JarReferenceGenerator(BlobReferenceGenerator.builder()
        .setAnkhInvokePackage(ankhInvokePackage)
        .setDisableClassRename(disableClassRename)
        .appendReferencePackage(referencePackages)
        .build());
    File tempJar = new File(cacheDirectory, "tmp.jar");
    tempJar.getParentFile().mkdirs();
    try {
      generator.execute(new ArrayList<File>(getInputJars().getFiles()), tempJar);
      Files.copy(tempJar, getOutputJar());
    } finally {
      if (tempJar.exists()) {
        tempJar.delete();
      }
    }
  }
}
