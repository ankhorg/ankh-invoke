package org.inksnow.ankhinvoke.map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class SpigotBuildData {
  private static final @NotNull Map<@NotNull String, @NotNull String> versionMap;

  static {
    Map<String, String> versionMapBuilder = new HashMap<>();
    URL url = AnkhInvokeMapping.class
        .getClassLoader()
        .getResource(AnkhInvokeMapping.ANKH_INVOKE_PACKAGE.replace('.', '/') + "/spigot-build-data");
    if (url == null) {
      throw new IllegalStateException("Spigot build data not found");
    }
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith("#")) {
          continue;
        }
        int splitIndex = line.indexOf(' ');
        versionMapBuilder.put(line.substring(0, splitIndex), line.substring(splitIndex + 1));
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    versionMap = Collections.unmodifiableMap(versionMapBuilder);
  }

  private SpigotBuildData() {
    throw new UnsupportedOperationException();
  }

  public static @NotNull String requireVersion(@NotNull String version) {
    String hash = versionMap.get(version);
    if (hash == null) {
      throw new IllegalArgumentException("Minecraft version " + version + " not found in Spigot BuildData");
    }
    return hash;
  }

  public static @Nullable String getVersion(@NotNull String version) {
    return versionMap.get(version);
  }
}
