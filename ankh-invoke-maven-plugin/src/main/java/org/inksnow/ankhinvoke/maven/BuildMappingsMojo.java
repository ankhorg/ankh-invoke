package org.inksnow.ankhinvoke.maven;

import com.google.common.io.Files;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.inksnow.ankhinvoke.codec.MappingRegistry;
import org.inksnow.ankhinvoke.codec.TextMetadata;
import org.inksnow.ankhinvoke.map.BlobMappingGenerator;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.zip.GZIPOutputStream;

@Mojo(name = "build-mappings", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class BuildMappingsMojo extends AbstractMojo {
  private static final @NotNull String DEFAULT_ANKH_INVOKE_PACKAGE = "org.inksnow.ankhinvoke";
  private static final @NotNull Function<String, Map<String, List<AiInjectBean>>> LINKED_HASH_MAP_NEW = it->new LinkedHashMap<>();
  private static final @NotNull Function<String, List<AiInjectBean>> ARRAY_LIST_NEW = it->new ArrayList<>();

  @Parameter(defaultValue = "${project}", readonly = true)
  private MavenProject project;

  @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
  private File outputDirectory;

  @Parameter(defaultValue = "${project.build.directory}/maven-status/ankh-invoke-cache", readonly = true)
  private File cacheDirectory;

  @Parameter
  private String ankhInvokePackage;

  @Parameter
  private List<AiInjectBean> mappings;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    String packageInternalName = (ankhInvokePackage == null ? DEFAULT_ANKH_INVOKE_PACKAGE : ankhInvokePackage)
        .replace('.', '/');

    Map<String, Map<String, List<AiInjectBean>>> registryMap = new LinkedHashMap<>();
    for (AiInjectBean injectBean : mappings) {
      Map<String, List<AiInjectBean>> configSetMap = registryMap.computeIfAbsent(injectBean.getRegistry(), LINKED_HASH_MAP_NEW);
      List<AiInjectBean> configSet = configSetMap.computeIfAbsent(injectBean.getMapSet(), ARRAY_LIST_NEW);
      configSet.add(injectBean);
    }

    TextMetadata.Builder metadata = TextMetadata.builder();
    File metadataFile = new File(outputDirectory, packageInternalName + "/metadata.txt");
    if (metadataFile.exists()) {
      try(InputStream in = new FileInputStream(metadataFile)) {
        metadata.load(in);
      } catch (IOException e) {
        throw new MojoExecutionException(e);
      }
    }

    for (Map.Entry<String, Map<String, List<AiInjectBean>>> registryEntry : registryMap.entrySet()) {
      String registry = registryEntry.getKey();
      metadata.append("mappings registry: " + registry);
      File registryDirectory = new File(outputDirectory, packageInternalName + "/" + registry);
      MappingRegistry.Builder registryBuilder = MappingRegistry.builder();

      for (Map.Entry<String, List<AiInjectBean>> mapSetEntry : registryEntry.getValue().entrySet()) {
        String mapSet = mapSetEntry.getKey();
        metadata.append("  |- map set: " + mapSet);
        MappingRegistry.MappingSet.Builder mapSetBuilder = registryBuilder.appendSet()
            .setName(mapSet);

        for (AiInjectBean injectBean : mapSetEntry.getValue()) {
          String[] predicates = injectBean.getPredicates() == null ? new String[0] : injectBean.getPredicates();
          metadata.append("    |- map entry: " + injectBean.getName())
              .append("      |- minecraft version: " + injectBean.getMinecraftVersion())
              .append("      |- build dat hash: " + injectBean.getBuildDataHash())
              .append("      |- is use spigot mapping: " + injectBean.isUseSpigotMapping());
          for (String predicate : predicates) {
            metadata.append("      |- predicate: " + predicate);
          }
          mapSetBuilder.appendEntry()
              .setName(injectBean.getName())
              .appendPredicate(predicates);

          BlobMappingGenerator generator = BlobMappingGenerator.builder()
              .setCacheDirectory(cacheDirectory)
              .setTargetFile(new File(registryDirectory, "mappings/" + injectBean.getName()))
              .setLogFunction(getLog()::info)
              .setMinecraftVersion(injectBean.getMinecraftVersion())
              .setBuildDataHash(injectBean.getBuildDataHash())
              .setUseSpigotMapping(injectBean.isUseSpigotMapping())
              .build();

          try {
            generator.run();
          }catch (IOException e) {
            throw new MojoExecutionException(e);
          }
        }
      }

      MappingRegistry mappingRegistry = registryBuilder.build();
      try(DataOutputStream out =  new DataOutputStream(new GZIPOutputStream(new FileOutputStream(new File(registryDirectory, "map-registry"))))) {
        mappingRegistry.save(out);
      } catch (IOException e) {
        throw new MojoExecutionException(e);
      }

      metadata.append("ended mappings registry: " + registry);
    }

    try {
      Files.createParentDirs(metadataFile);
      try (OutputStream out = new FileOutputStream(metadataFile)) {
        metadata.build().save(out);
      }
    } catch (IOException e) {
      throw new MojoExecutionException(e);
    }
  }
}
