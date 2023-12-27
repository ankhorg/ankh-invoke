package org.inksnow.ankhinvoke.gradle;

import com.google.common.io.Files;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.inksnow.ankhinvoke.codec.MappingRegistry;
import org.inksnow.ankhinvoke.codec.TextMetadata;
import org.inksnow.ankhinvoke.map.BlobMappingGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.zip.GZIPOutputStream;

public class BuildMappingsTask extends DefaultTask {
  private static final @NotNull Function<@NotNull String, @NotNull Map<@NotNull String, @NotNull List<@NotNull AiInjectBean>>> LINKED_HASH_MAP_NEW = it->new LinkedHashMap<>();
  private static final @NotNull Function<@NotNull String, @NotNull List<@NotNull AiInjectBean>> ARRAY_LIST_NEW = it->new ArrayList<>();

  private @NotNull String ankhInvokePackage = AnkhInjectorPlugin.ANKH_INVOKE_PACKAGE;
  private @NotNull String registryName = "default";
  private @NotNull List<@NotNull AiInjectBean> mappings = new ArrayList<>();
  private @Nullable File outputDirectory;

  public BuildMappingsTask() {
    doLast(it-> {
      try {
        this.execute();
      } catch (RuntimeException | Error e) {
        throw e;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  @Input
  public @NotNull String getAnkhInvokePackage() {
    return ankhInvokePackage;
  }

  public void setAnkhInvokePackage(@NotNull String ankhInvokePackage) {
    this.ankhInvokePackage = ankhInvokePackage;
  }

  @Input
  public @NotNull String getRegistryName() {
    return registryName;
  }

  public void setRegistryName(@NotNull String registryName) {
    this.registryName = registryName;
  }

  @Input
  public @NotNull List<@NotNull AiInjectBean> getMappings() {
    return Collections.unmodifiableList(mappings);
  }

  public void mapping(@NotNull AiInjectBean injectBean) {
    mappings.add(injectBean);
  }


  public void mapping(@NotNull String mapSet, @NotNull String minecraftVersion, @Nullable Action<@NotNull AiInjectBean> action) {
    AiInjectBean bean = new AiInjectBean();
    bean.setRegistry(getRegistryName());
    bean.setMapSet(mapSet);
    bean.setMinecraftVersion(minecraftVersion);
    mappings.add(bean);
    if (action != null) {
      action.execute(bean);
    }
    if (bean.getName() == null) {
      bean.setName(minecraftVersion +
          (bean.getBuildDataHash() == null ? "-latest" : "-" + bean.getBuildDataHash()) +
          (bean.isUseSpigotMapping() ? "-spigot" : "-mojang"));
    }
  }

  @OutputDirectory
  public @NotNull File getOutputDirectory() {
    if (outputDirectory == null) {
      throw new IllegalStateException("missing require property 'outputDirectory'");
    }
    return outputDirectory;
  }

  public void setOutputDirectory(@NotNull File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  private void execute() throws Exception {
    File cacheDirectory = new File(getProject().getBuildDir(), "ankh-invoke-cache");
    String packageInternalName = ankhInvokePackage.replace('.', '/');

    Map<String, Map<String, List<AiInjectBean>>> registryMap = new LinkedHashMap<>();
    for (AiInjectBean injectBean : mappings) {
      Map<String, List<AiInjectBean>> configSetMap = registryMap.computeIfAbsent(injectBean.getRegistry(), LINKED_HASH_MAP_NEW);
      List<AiInjectBean> configSet = configSetMap.computeIfAbsent(injectBean.getMapSet(), ARRAY_LIST_NEW);
      configSet.add(injectBean);
    }

    TextMetadata.Builder metadata = TextMetadata.builder();
    File metadataFile = new File(getOutputDirectory(), packageInternalName + "/metadata.txt");
    if (metadataFile.exists()) {
      try(InputStream in = new FileInputStream(metadataFile)) {
        metadata.load(in);
      }
    }

    for (Map.Entry<String, Map<String, List<AiInjectBean>>> registryEntry : registryMap.entrySet()) {
      String registry = registryEntry.getKey();
      metadata.append("mappings registry: " + registry);
      File registryDirectory = new File(getOutputDirectory(), packageInternalName + "/" + registry);
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
              .setLogFunction(getLogger()::warn)
              .setMinecraftVersion(injectBean.getMinecraftVersion())
              .setBuildDataHash(injectBean.getBuildDataHash())
              .setUseSpigotMapping(injectBean.isUseSpigotMapping())
              .build();

          generator.run();
        }
      }

      MappingRegistry mappingRegistry = registryBuilder.build();
      try(DataOutputStream out =  new DataOutputStream(new GZIPOutputStream(new FileOutputStream(new File(registryDirectory, "map-registry"))))) {
        mappingRegistry.save(out);
      }

      metadata.append("ended mappings registry: " + registry);
    }

    Files.createParentDirs(metadataFile);
    try (OutputStream out = new FileOutputStream(metadataFile)) {
      metadata.build().save(out);
    }
  }
}
