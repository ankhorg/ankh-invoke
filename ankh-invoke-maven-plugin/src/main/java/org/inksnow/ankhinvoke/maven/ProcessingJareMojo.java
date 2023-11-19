package org.inksnow.ankhinvoke.maven;

import com.google.common.io.Files;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.inksnow.ankhinvoke.map.BlobReferenceGenerator;
import org.inksnow.ankhinvoke.map.JarReferenceGenerator;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Mojo(name = "apply-reference-jar", defaultPhase = LifecyclePhase.PACKAGE)
public class ProcessingJareMojo extends AbstractMojo {
  private static final @NotNull String DEFAULT_ANKH_INVOKE_PACKAGE = "org.inksnow.ankhinvoke";
  @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}-shaded.jar", readonly = true)
  private File processJar;

  @Parameter(defaultValue = "${project.build.directory}/maven-status/ankh-invoke-cache", readonly = true)
  private File cacheDirectory;

  @Parameter
  private String ankhInvokePackage;

  @Parameter
  private List<String> referencePackages;

  @Parameter
  private boolean disableClassRename;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    String ankhInvokePackage = this.ankhInvokePackage == null ? DEFAULT_ANKH_INVOKE_PACKAGE : this.ankhInvokePackage;
    JarReferenceGenerator generator = new JarReferenceGenerator(BlobReferenceGenerator.builder()
        .setAnkhInvokePackage(ankhInvokePackage)
        .setDisableClassRename(disableClassRename)
        .appendReferencePackage(referencePackages)
        .build());
    File tempJar = new File(cacheDirectory, "tmp.jar");
    tempJar.getParentFile().mkdirs();
    try {
      generator.execute(processJar, tempJar);
      Files.copy(tempJar, processJar);
    } catch (IOException e) {
      throw new MojoFailureException(e);
    } finally {
      if (tempJar.exists()) {
        tempJar.delete();
      }
    }
  }
}
