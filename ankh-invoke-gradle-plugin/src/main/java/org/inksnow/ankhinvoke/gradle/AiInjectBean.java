package org.inksnow.ankhinvoke.gradle;

import java.util.Arrays;
import java.util.Objects;

public class AiInjectBean {
  private String registry;
  private String mapSet;
  private String name;
  private String minecraftVersion;
  private String buildDataHash;
  private String[] predicates;
  private boolean useSpigotMapping;

  public String getRegistry() {
    return registry;
  }

  public void setRegistry(String registry) {
    this.registry = registry;
  }

  public String getMapSet() {
    return mapSet;
  }

  public void setMapSet(String mapSet) {
    this.mapSet = mapSet;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getMinecraftVersion() {
    return minecraftVersion;
  }

  public void setMinecraftVersion(String minecraftVersion) {
    this.minecraftVersion = minecraftVersion;
  }

  public String getBuildDataHash() {
    return buildDataHash;
  }

  public void setBuildDataHash(String buildDataHash) {
    this.buildDataHash = buildDataHash;
  }

  public String[] getPredicates() {
    return predicates;
  }

  public void setPredicates(String[] predicates) {
    this.predicates = predicates;
  }

  public boolean isUseSpigotMapping() {
    return useSpigotMapping;
  }

  public void setUseSpigotMapping(boolean useSpigotMapping) {
    this.useSpigotMapping = useSpigotMapping;
  }

  @Override
  public String toString() {
    return "AiInjectBean{" +
        "registry='" + registry + '\'' +
        ", mapSet='" + mapSet + '\'' +
        ", name='" + name + '\'' +
        ", minecraftVersion='" + minecraftVersion + '\'' +
        ", buildDataHash='" + buildDataHash + '\'' +
        ", predicates=" + Arrays.toString(predicates) +
        ", useSpigotMapping=" + useSpigotMapping +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AiInjectBean that = (AiInjectBean) o;

    if (useSpigotMapping != that.useSpigotMapping) return false;
    if (!Objects.equals(registry, that.registry)) return false;
    if (!Objects.equals(mapSet, that.mapSet)) return false;
    if (!Objects.equals(name, that.name)) return false;
    if (!Objects.equals(minecraftVersion, that.minecraftVersion))
      return false;
    if (!Objects.equals(buildDataHash, that.buildDataHash)) return false;
    // Probably incorrect - comparing Object[] arrays with Arrays.equals
    return Arrays.equals(predicates, that.predicates);
  }

  @Override
  public int hashCode() {
    int result = registry != null ? registry.hashCode() : 0;
    result = 31 * result + (mapSet != null ? mapSet.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (minecraftVersion != null ? minecraftVersion.hashCode() : 0);
    result = 31 * result + (buildDataHash != null ? buildDataHash.hashCode() : 0);
    result = 31 * result + Arrays.hashCode(predicates);
    result = 31 * result + (useSpigotMapping ? 1 : 0);
    return result;
  }
}
