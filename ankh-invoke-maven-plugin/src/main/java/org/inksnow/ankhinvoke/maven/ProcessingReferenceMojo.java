package org.inksnow.ankhinvoke.maven;

import com.google.common.io.Files;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.inksnow.ankhinvoke.map.BlobReferenceGenerator;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.List;

@Mojo(name = "apply-transformer", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class ProcessingReferenceMojo extends AbstractMojo {
  private static final @NotNull String DEFAULT_ANKH_INVOKE_PACKAGE = "org.inksnow.ankhinvoke";
  @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
  private File outputDirectory;

  @Parameter
  private String ankhInvokePackage;

  @Parameter
  private List<String> referencePackages;

  @Parameter
  private boolean disableClassRename;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    String ankhInvokePackage = this.ankhInvokePackage == null ? DEFAULT_ANKH_INVOKE_PACKAGE : this.ankhInvokePackage;
    BlobReferenceGenerator generator = BlobReferenceGenerator.builder()
        .setAnkhInvokePackage(ankhInvokePackage)
        .setDisableClassRename(disableClassRename)
        .appendReferencePackage(referencePackages)
        .build();
    try {
      scan(outputDirectory, "", (file, name) -> {
        try (InputStream in = new FileInputStream(file)) {
          generator.acceptScan(name, in);
        }
      });
      scan(outputDirectory, "", (file, name) -> {
        try (InputStream in = new FileInputStream(file)) {
          BlobReferenceGenerator.ProcessAction processAction = generator.acceptProcess(name, in);
          if (processAction.isRemove()) {
            file.delete();
            return;
          }
          if (processAction.shouldReplace()) {
            Files.write(processAction.getBytes(), file);
          }
          if (processAction.shouldRename()){
            File targetFile = new File(outputDirectory, processAction.getNewName());
            Files.createParentDirs(targetFile);
            file.renameTo(targetFile);
          }
        }
      });
      for (BlobReferenceGenerator.ProcessAction processAction : generator.collect()) {
        if(processAction.isKeep() && processAction.shouldRename() && processAction.shouldReplace()) {
          File targetFile = new File(outputDirectory, processAction.getNewName());
          Files.createParentDirs(targetFile);
          Files.write(processAction.getBytes(), targetFile);
        } else if (processAction.isRemove() && processAction.shouldRename()){
          new File(outputDirectory, processAction.getNewName()).delete();
        }
      }
    } catch (IOException e) {
      throw new MojoExecutionException(e);
    }
  }

  private void scan(@NotNull File current, @NotNull String path, @NotNull ScanConsumer target) throws IOException {
    for (File file : current.listFiles()) {
      if (file.isDirectory()) {
        scan(file, path + file.getName() + "/", target);
      }else {
        target.accept(file, path + file.getName());
      }
    }
  }

  @FunctionalInterface
  private interface ScanConsumer {
    void accept(File file, String name) throws IOException;
  }
}
