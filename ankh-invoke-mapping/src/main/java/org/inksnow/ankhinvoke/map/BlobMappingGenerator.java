package org.inksnow.ankhinvoke.map;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.md_5.specialsource.Jar;
import net.md_5.specialsource.JarMapping;
import net.md_5.specialsource.JarRemapper;
import net.md_5.specialsource.provider.JarProvider;
import net.md_5.specialsource.provider.JointProvider;
import org.inksnow.ankhinvoke.codec.BlobMap;
import org.inksnow.ankhinvoke.codec.util.NoCloseInputStream;
import org.inksnow.ankhinvoke.map.bean.ClassBean;
import org.inksnow.ankhinvoke.map.bean.FieldBean;
import org.inksnow.ankhinvoke.map.bean.MethodBean;
import org.inksnow.ankhinvoke.map.util.AiStringUtils;
import org.inksnow.ankhinvoke.map.util.LineBufferPrintStream;
import org.inksnow.ankhinvoke.map.util.SafeIoUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.GZIPOutputStream;

public final class BlobMappingGenerator {
  private static final @NotNull String SPIGOT_BUILD_DATA_URL = "https://hub.spigotmc.org/stash/projects/SPIGOT/repos/builddata/raw/";
  private static final @NotNull Gson gson = new GsonBuilder().create();

  private final @NotNull Consumer<String> logFunction;
  private final @NotNull File cacheDirectory;
  private final boolean disableCache;
  private final @NotNull File targetFile;
  private final @NotNull String minecraftVersion;
  private final @NotNull String buildDataHash;
  private final boolean useSpigotMapping;

  private final @NotNull JointProvider inheritanceProviders;
  private final @NotNull JarMapping spigotJarMapping;
  private final @NotNull JarMapping mojangJarMapping;
  private final @NotNull JarRemapper spigotRemapper;
  private final @NotNull JarRemapper mojangRemapper;
  private final @NotNull Map<@NotNull String, @NotNull ClassBean> classBeanMap;

  private BlobMappingGenerator(@NotNull Consumer<String> logFunction, @NotNull File cacheDirectory, boolean disableCache, @NotNull File targetFile, @NotNull String minecraftVersion, @NotNull String buildDataHash, boolean useSpigotMapping) {
    this.logFunction = logFunction;
    this.cacheDirectory = cacheDirectory;
    this.disableCache = disableCache;
    this.targetFile = targetFile;
    this.minecraftVersion = minecraftVersion;
    this.buildDataHash = buildDataHash;
    this.useSpigotMapping = useSpigotMapping;

    this.inheritanceProviders = new JointProvider();
    this.spigotJarMapping = new JarMapping();
    this.spigotJarMapping.setFallbackInheritanceProvider(this.inheritanceProviders);
    this.mojangJarMapping = new JarMapping();
    this.mojangJarMapping.setFallbackInheritanceProvider(this.inheritanceProviders);
    this.spigotRemapper = new JarRemapper(spigotJarMapping);
    this.mojangRemapper = new JarRemapper(mojangJarMapping);
    this.classBeanMap = new HashMap<>();
  }

  public static void main(String[] args) throws IOException {
    builder()
        .setCacheDirectory(new File("/home/inkerbot/IdeaProjects/ankh-invoke-dev/ankh-invoke-nbt/target/maven-status/ankh-invoke-cache"))
        .setTargetFile(new File("/home/inkerbot/IdeaProjects/ankh-invoke-dev/ankh-invoke-nbt/target/classes/org/inksnow/ankhinvoke/ankh-invoke-nbt/mappings/mojang-1.20.2"))
        .setUseSpigotMapping(false)
        .setLogFunction(System.out::println)
        .setMinecraftVersion("1.20.2")
        .setBuildDataHash("172197ceb99364701937947ea7fc424ecf1bb694")
        .build()
        .run();
  }

  private void saveBlobMap(File cacheTmpFile, File cacheFile) throws IOException {
    BlobMap blobMap = createBlobMap();
    Files.createParentDirs(cacheTmpFile);
    try(DataOutputStream out = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(cacheTmpFile)))) {
      blobMap.save(out);
    }
    Files.createParentDirs(cacheFile);
    if (cacheFile.exists()) {
      if (!cacheFile.delete()) {
        throw new IOException("Failed to delete " + cacheFile);
      }
    }
    if (!cacheTmpFile.renameTo(cacheFile)) {
      throw new IOException("[saveBlobMap] Failed to rename " + cacheTmpFile + " to " + cacheFile);
    }
    Files.createParentDirs(targetFile);
    Files.copy(cacheFile, targetFile);
  }

  private @NotNull BlobMap createBlobMap() {
    BlobMap.Builder builder = BlobMap.builder();
    for (Map.Entry<String, ClassBean> classEntry : classBeanMap.entrySet()) {
      builder.appendClass(classEntry.getKey(), classEntry.getValue().remapped());
      for (Map.Entry<FieldBean, String> fieldEntry : classEntry.getValue().fieldMap().entrySet()) {
        builder.appendField(classEntry.getKey(), fieldEntry.getKey().name(), fieldEntry.getKey().desc(), fieldEntry.getValue());
      }
      for (Map.Entry<MethodBean, String> methodEntry : classEntry.getValue().methodMap().entrySet()) {
        builder.appendMethod(classEntry.getKey(), methodEntry.getKey().name(), methodEntry.getKey().desc(), methodEntry.getValue());
      }
    }
    return builder.build();
  }

  private void loadMappings(JsonObject buildDataInfo) throws IOException {
    String serverUrl = buildDataInfo.get("serverUrl").getAsString();
    String mappingsUrl = buildDataInfo.get("mappingsUrl").getAsString();

    inheritanceProviders.add(new JarProvider(Jar.init(getFile(null, serverUrl))));

    try (BufferedReader reader = getReader(null, mappingsUrl)) {
      spigotJarMapping.loadMappings(reader, null, null, false);
    }

    try (BufferedReader reader = getReader(null, mappingsUrl)) {
      mojangJarMapping.loadMappings(reader, null, null, false);
    }

    JsonElement classMappingsElement = buildDataInfo.get("classMappings");
    if (classMappingsElement != null) {
      try (BufferedReader reader = getReader(buildDataHash, "mappings/" + classMappingsElement.getAsString())) {
        spigotJarMapping.loadMappings(reader, null, null, false);
      }
    }

    JsonElement memberMappingsElement = buildDataInfo.get("memberMappings");
    if (memberMappingsElement != null) {
      try (BufferedReader reader = getReader(buildDataHash, "mappings/" + memberMappingsElement.getAsString())) {
        spigotJarMapping.loadMappings(reader, null, null, false);
      }
    }

    JsonElement packageMappingsElement = buildDataInfo.get("packageMappings");
    if (packageMappingsElement != null) {
      try (BufferedReader reader = getReader(buildDataHash, "mappings/" + packageMappingsElement.getAsString())) {
        spigotJarMapping.loadMappings(reader, null, null, false);
      }
    }
  }

  private void scanJar(@NotNull JarInputStream in) throws IOException {
    JarEntry entry;
    while ((entry = in.getNextJarEntry()) != null) {
      if (entry.isDirectory()) {
        continue;
      }
      if (entry.getName().endsWith(".jar")) {
        log("process nested jar: ", entry.getName());
        try (JarInputStream jarIn = new JarInputStream(new NoCloseInputStream(in))) {
          scanJar(jarIn);
        }
        continue;
      }
      if (entry.getName().endsWith(".class")) {
        try(InputStream classIn = new NoCloseInputStream(in)) {
          scanClass(classIn);
        }catch (Exception e) {
          log("Failed to scan class, skipped: ", entry.getName(), e);
        }
      }
    }
  }

  public void run() throws IOException {
    log("generate reobf mappings");
    String fileName = minecraftVersion + "_" + buildDataHash + "_" + (useSpigotMapping ? "spigot" : "mojang");
    File cacheFile = getCacheFile("blobmap", fileName, false);
    File cacheTmpFile = getCacheFile("blobmap", fileName, true);
    if (false && !disableCache) {
      if(targetFile.exists()) {
        return;
      } else if (cacheFile.exists()) {
        Files.createParentDirs(targetFile);
        Files.copy(cacheFile, targetFile);
        return;
      }
    }

    JsonObject buildDataInfo;
    try (BufferedReader reader = getReader(buildDataHash, "info.json")) {
      buildDataInfo = gson.fromJson(reader, JsonObject.class);
    }
    String minecraftVersion = buildDataInfo.get("minecraftVersion").getAsString();
    if (!this.minecraftVersion.startsWith(minecraftVersion)) {
      throw new IllegalStateException("Version mismatch: " + this.minecraftVersion + " != " + minecraftVersion);
    }

    String serverUrl = buildDataInfo.get("serverUrl").getAsString();

    loadMappings(buildDataInfo);

    try (JarInputStream in = new JarInputStream(new FileInputStream(getFile(null, serverUrl)))) {
      scanJar(in);
    }
    saveBlobMap(cacheTmpFile, cacheFile);
    Files.copy(cacheFile, targetFile);
    log("generate success, saved to ", targetFile);
  }

  private @NotNull File getFile(@Nullable String hash, @NotNull String path) throws IOException {
    URL url;
    File cacheFile, cacheTmpFile;
    if (hash == null) {
      url = new URL(path);
      String fileName = AiStringUtils.substringAfterLastOrdinal(path, "/", 2);
      cacheFile = getCacheFile("mojang", fileName, false);
      cacheTmpFile = getCacheFile("mojang", fileName, true);
    } else {
      url = new URL(SPIGOT_BUILD_DATA_URL + path + "?at=" + hash);
      String fileName = path;
      cacheFile = getCacheFile("spigot:" + hash, fileName, false);
      cacheTmpFile = getCacheFile("spigot:" + hash, fileName, true);
    }

    if (cacheFile.exists() && !disableCache) {
      return cacheFile;
    }
    log("downloading: ", url, " to ", cacheFile);
    URLConnection connection = url.openConnection();
    if (connection instanceof HttpURLConnection) {
      HttpURLConnection httpConnection = (HttpURLConnection) connection;
      httpConnection.setInstanceFollowRedirects(false);
      int responseCode = httpConnection.getResponseCode();
      if (responseCode == 404) {
        throw new FileNotFoundException("File not found: " + url);
      } else if (responseCode >= 300) {
        throw new IOException("Unexpected response code: " + responseCode + " " + httpConnection.getResponseMessage());
      }
    }

    Files.createParentDirs(cacheTmpFile);
    try (InputStream in = connection.getInputStream()) {
      try (OutputStream out = new FileOutputStream(cacheTmpFile, false)) {
        ByteStreams.copy(in, out);
      }
    }

    Files.createParentDirs(cacheFile);
    if (!cacheTmpFile.renameTo(cacheFile)) {
      throw new IOException("[getFile] Failed to rename " + cacheTmpFile + " to " + cacheFile);
    }
    return cacheFile;
  }

  private @NotNull File getCacheFile(@NotNull String cacheKey, @NotNull String name, boolean isTmp) {
    cacheKey = SafeIoUtils.safeFileName(cacheKey.replace('-', '_'));
    name = SafeIoUtils.safeFileName(name);
    return new File(cacheDirectory, cacheKey + "-" + name + (isTmp ? ".tmp" : ""));
  }

  private @NotNull BufferedReader getReader(@Nullable String hash, @NotNull String path) throws IOException {
    return new BufferedReader(new InputStreamReader(new FileInputStream(getFile(hash, path)), StandardCharsets.UTF_8));
  }

  private void scanClass(@NotNull InputStream in) throws IOException {
    ClassNode classNode = new ClassNode();
    new ClassReader(in)
        .accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

    JarRemapper remapper = useSpigotMapping ? spigotRemapper : mojangRemapper;
    String mappedClassName;
    String spigotClassName;
    if (classNode.nestHostClass != null && classNode.name.startsWith(classNode.nestHostClass + "$")) {
      String innerName = classNode.name.substring(classNode.nestHostClass.length() + 1);
      String mappedHostClassName = remapper.map(classNode.nestHostClass);
      String spigotHostClassName = spigotRemapper.map(classNode.nestHostClass);

      String mappedInnerClassName = remapper.mapInnerClassName(classNode.name, classNode.nestHostClass, innerName);
      String spigotInnerClassName = spigotRemapper.mapInnerClassName(classNode.name, classNode.nestHostClass, innerName);

      mappedClassName = mappedHostClassName + "$" + (mappedInnerClassName.isEmpty() ? innerName : mappedInnerClassName);
      spigotClassName = spigotHostClassName + "$" + (spigotInnerClassName.isEmpty() ? innerName : spigotInnerClassName);

      if (AnkhInvokeMapping.DEBUG) {
        log("inner class found: ", classNode.name, " with host ", classNode.nestHostClass);
      }
    } else {
      mappedClassName = remapper.map(classNode.name);
      spigotClassName = spigotRemapper.map(classNode.name);
    }
    ClassBean classBean = classBeanMap.getOrDefault(mappedClassName, new ClassBean(mappedClassName, spigotClassName));

    if (AnkhInvokeMapping.DEBUG) {
      log("mapped class name: ", mappedClassName, " -> ", spigotClassName);
    }
    for (FieldNode field : classNode.fields) {
      String mappedFieldName = remapper.mapFieldName(classNode.name, field.name, field.desc);

      if (!mappedFieldName.equals(field.name)) {
        String mappedFieldDesc = remapper.mapDesc(field.desc);
        classBean.fieldMap().put(new FieldBean(mappedClassName, mappedFieldName, mappedFieldDesc), field.name);
      }
    }

    for (MethodNode methodNode : classNode.methods) {
      String mappedMethodName = remapper.mapMethodName(classNode.name, methodNode.name, methodNode.desc);

      if (!mappedMethodName.equals(methodNode.name)) {
        String mappedMethodDesc = mojangRemapper.mapDesc(methodNode.desc);
        classBean.methodMap().put(new MethodBean(mappedClassName, mappedMethodName, mappedMethodDesc), methodNode.name);
      }
    }

    if (!classBean.raw().endsWith(classBean.remapped()) || !classBean.fieldMap().isEmpty() || !classBean.methodMap().isEmpty()) {
      classBeanMap.put(mappedClassName, classBean);
    }
  }

  public static @NotNull Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private @NotNull Consumer<@NotNull String> logFunction = System.out::println;
    private @Nullable File cacheDirectory;
    private @Nullable File targetFile;
    private @Nullable String minecraftVersion;
    private @Nullable String buildDataHash;
    private boolean useSpigotMapping;
    private boolean disableCache;

    public @NotNull Builder setLogFunction(Consumer<String> logFunction) {
      this.logFunction = logFunction;
      return this;
    }

    public @NotNull Builder setCacheDirectory(@NotNull File cacheDirectory) {
      this.cacheDirectory = cacheDirectory;
      return this;
    }

    public @NotNull Builder setTargetFile(@NotNull File targetFile) {
      this.targetFile = targetFile;
      return this;
    }

    public @NotNull Builder setMinecraftVersion(@NotNull String minecraftVersion) {
      this.minecraftVersion = minecraftVersion;
      return this;
    }

    public @NotNull Builder setBuildDataHash(@Nullable String buildDataHash) {
      this.buildDataHash = buildDataHash;
      return this;
    }

    public @NotNull Builder setUseSpigotMapping() {
      this.useSpigotMapping = true;
      return this;
    }

    public @NotNull Builder setUseSpigotMapping(boolean useSpigotMapping) {
      this.useSpigotMapping = useSpigotMapping;
      return this;
    }

    public @NotNull Builder setDisableCache() {
      this.disableCache = true;
      return this;
    }

    public @NotNull Builder setDisableCache(boolean disableCache) {
      this.disableCache = disableCache;
      return this;
    }

    public @NotNull BlobMappingGenerator build() {
      if (cacheDirectory == null) {
        throw new IllegalStateException("cacheDirectory is null");
      }
      if (targetFile == null) {
        throw new IllegalStateException("targetFile is null");
      }
      if (minecraftVersion == null) {
        throw new IllegalStateException("minecraftVersion is null");
      }
      if (buildDataHash == null) {
        buildDataHash = SpigotBuildData.requireVersion(minecraftVersion);
      }
      return new BlobMappingGenerator(logFunction, cacheDirectory, disableCache, targetFile, minecraftVersion, buildDataHash, useSpigotMapping);
    }
  }

  private void log(@NotNull Object @NotNull ... messages) {
    StringBuilder builder = new StringBuilder();
    Throwable e = null;
    for (int i = 0; i < messages.length; i++) {
      Object message = messages[i];
      if (i == messages.length - 1 && message instanceof Throwable) {
        e = (Throwable) message;
      } else {
        builder.append(message);
      }
    }
    logFunction.accept(builder.toString());
    if (e != null) {
      try (PrintStream printStream = new LineBufferPrintStream(logFunction)) {
        e.printStackTrace(printStream);
      }
    }
  }
}
