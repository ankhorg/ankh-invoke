package org.inksnow.ankhinvoke.codec;

import org.inksnow.ankhinvoke.codec.util.ReferenceParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class BlobMap {
  private static final Function<String, List<String[]>> ARRAY_LIST_NEW = it -> new ArrayList<>();
  private final @NotNull Metadata metadata;
  private final @NotNull Map<@NotNull String, @NotNull String> classMap;
  private final @NotNull Map<@NotNull String, @NotNull String> fieldMap;
  private final @NotNull Map<@NotNull String, @NotNull String> methodMap;

  private BlobMap(@NotNull Metadata metadata, @NotNull Map<@NotNull String, @NotNull String> classMap, @NotNull Map<@NotNull String, @NotNull String> fieldMap, @NotNull Map<@NotNull String, @NotNull String> methodMap) {
    this.metadata = metadata;
    this.classMap = classMap;
    this.fieldMap = fieldMap;
    this.methodMap = methodMap;
  }

  public @NotNull Metadata metadata() {
    return metadata;
  }

  public @NotNull Map<@NotNull String, @NotNull String> classMap() {
    return classMap;
  }

  public @NotNull Map<@NotNull String, @NotNull String> fieldMap() {
    return fieldMap;
  }

  public @NotNull Map<@NotNull String, @NotNull String> methodMap() {
    return methodMap;
  }

  public static @NotNull BlobMap load(@NotNull DataInputStream in) throws IOException {
    Metadata metadata = Metadata.load(in);
    BlobConstPool constPool = BlobConstPool.load(in);
    int classMapSize = in.readInt();
    int fieldMapSize = in.readInt();
    int methodMapSize = in.readInt();
    Map<String, String> classMap = new HashMap<>(classMapSize);
    Map<String, String> fieldMap = new HashMap<>(fieldMapSize);
    Map<String, String> methodMap = new HashMap<>(methodMapSize);
    for (int i = 0; i < classMapSize; i++) {
      String rawName = constPool.get(in.readInt());
      classMap.put(rawName, constPool.get(in.readInt()));
      String ownedName = "L" + rawName + ";";
      int fieldCount = in.readInt();
      for (int j = 0; j < fieldCount; j++) {
        fieldMap.put(ownedName + constPool.get(in.readInt()) + ":" + constPool.get(in.readInt()), constPool.get(in.readInt()));
      }
      int methodCount = in.readInt();
      for (int j = 0; j < methodCount; j++) {
        methodMap.put(ownedName + constPool.get(in.readInt()) + constPool.get(in.readInt()), constPool.get(in.readInt()));
      }
    }
    return new BlobMap(metadata, Collections.unmodifiableMap(classMap), Collections.unmodifiableMap(fieldMap), Collections.unmodifiableMap(methodMap));
  }

  private static @NotNull Map<@NotNull String, List<@NotNull String @NotNull []>> computeBeanMap(
      BlobConstPool.@NotNull Builder constPoolBuilder,
      @NotNull Map<@NotNull String, @NotNull String> classMap,
      @NotNull Map<@NotNull String, @NotNull String> sourceMap,
      boolean isField) {
    Map<String, List<String[]>> beanMap = new HashMap<>(classMap.size());
    for (Map.Entry<String, String> entry : sourceMap.entrySet()) {
      String[] key = isField ? ReferenceParser.parseField(entry.getKey()) : ReferenceParser.parseMethod(entry.getKey());
      beanMap.computeIfAbsent(key[0], ARRAY_LIST_NEW).add(new String[]{key[1], key[2], entry.getValue()});
      classMap.putIfAbsent(key[0], key[0]);
      constPoolBuilder.append(key[1], key[2], entry.getValue());
    }
    return beanMap;
  }

  private static void countClassMap(
      BlobConstPool.@NotNull Builder constPoolBuilder,
      @NotNull Map<@NotNull String, @NotNull String> classMap) {
    for (Map.Entry<String, String> entry : classMap.entrySet()) {
      constPoolBuilder.append(entry.getKey(), entry.getValue());
    }
  }

  private static void saveBeanList(
      @NotNull DataOutputStream out,
      @NotNull BlobConstPool constPool,
      @Nullable List<@NotNull String @NotNull []> beanList) throws IOException {
    if (beanList != null) {
      out.writeInt(beanList.size());
      for (String[] bean : beanList) {
        out.writeInt(constPool.getKey(bean[0]));
        out.writeInt(constPool.getKey(bean[1]));
        out.writeInt(constPool.getKey(bean[2]));
      }
    } else {
      out.writeInt(0);
    }
  }

  public static @NotNull Builder builder() {
    return new Builder();
  }

  public void save(@NotNull DataOutputStream out) throws IOException {
    BlobConstPool.Builder constPoolBuilder = BlobConstPool.builder();
    Map<String, String> classMap = new HashMap<>(this.classMap);
    Map<String, List<String[]>> parsedFields = computeBeanMap(constPoolBuilder, classMap, fieldMap, true);
    Map<String, List<String[]>> parsedMethods = computeBeanMap(constPoolBuilder, classMap, methodMap, false);
    countClassMap(constPoolBuilder, classMap);

    BlobConstPool constPool = constPoolBuilder.build();

    metadata.save(out);
    constPool.save(out);

    out.writeInt(classMap.size());
    out.writeInt(fieldMap.size());
    out.writeInt(methodMap.size());

    for (Map.Entry<String, String> classEntry : classMap.entrySet()) {
      out.writeInt(constPool.getKey(classEntry.getKey()));
      out.writeInt(constPool.getKey(classEntry.getValue()));
      saveBeanList(out, constPool, parsedFields.get(classEntry.getKey()));
      saveBeanList(out, constPool, parsedMethods.get(classEntry.getKey()));
    }
  }

  public @Nullable String mapClass(@NotNull String name) {
    return classMap.get(name);
  }

  public @Nullable String mapFieldName(@NotNull String owner, @NotNull String name, @NotNull String desc) {
    return fieldMap.get("L" + owner + ";" + name + ":" + desc);
  }

  public @Nullable String mapMethodName(@NotNull String owner, @NotNull String name, @NotNull String desc) {
    return methodMap.get("L" + owner + ";" + name + desc);
  }

  public static final class Builder {
    private final Metadata.@NotNull Builder metadata = new Metadata.Builder(this);
    private final @NotNull Map<@NotNull String, @NotNull String> classMap = new HashMap<>();
    private final @NotNull Map<@NotNull String, @NotNull String> fieldMap = new HashMap<>();
    private final @NotNull Map<@NotNull String, @NotNull String> methodMap = new HashMap<>();

    public Metadata.@NotNull Builder metadata() {
      return metadata;
    }

    public @NotNull Builder appendClass(@NotNull String rawName, @NotNull String remapName) {
      classMap.put(rawName, remapName);
      return this;
    }

    public @NotNull Builder appendField(@NotNull String owner, @NotNull String name, @NotNull String desc, @NotNull String remapName) {
      fieldMap.put("L" + owner + ";" + name + ":" + desc, remapName);
      classMap.putIfAbsent(owner, owner);
      return this;
    }

    public @NotNull Builder appendMethod(@NotNull String owner, @NotNull String name, @NotNull String desc, @NotNull String remapName) {
      methodMap.put("L" + owner + ";" + name + desc, remapName);
      classMap.putIfAbsent(owner, owner);
      return this;
    }

    public @NotNull BlobMap build() {
      return new BlobMap(metadata.buildInternal(), Collections.unmodifiableMap(classMap), Collections.unmodifiableMap(fieldMap), Collections.unmodifiableMap(methodMap));
    }
  }

  public static final class Metadata {
    private final @NotNull List<@NotNull String> predicates;

    private Metadata(@NotNull List<@NotNull String> predicates) {
      this.predicates = predicates;
    }

    public @NotNull List<@NotNull String> predicates() {
      return predicates;
    }

    public static @NotNull Metadata load(@NotNull DataInputStream in) throws IOException {
      int predicateCount = in.readInt();
      String[] predicates = new String[predicateCount];
      for (int i = 0; i < predicateCount; i++) {
        predicates[i] = in.readUTF();
      }
      return new Metadata(
          Collections.unmodifiableList(Arrays.asList(predicates)));
    }

    public void save(@NotNull DataOutputStream out) throws IOException {
      out.writeInt(predicates.size());
      for (String predicate : predicates) {
        out.writeUTF(predicate);
      }
    }

    public static final class Builder {
      private final BlobMap.@NotNull Builder blobMapBuilder;
      private final @NotNull List<@NotNull String> predicates = new ArrayList<>();

      private Builder(BlobMap.@NotNull Builder blobMapBuilder) {
        this.blobMapBuilder = blobMapBuilder;
      }

      public @NotNull Builder appendPredicate(@NotNull String predicate) {
        predicates.add(predicate);
        return this;
      }

      public BlobMap.@NotNull Builder build() {
        return blobMapBuilder;
      }

      private @NotNull Metadata buildInternal() {
        return new Metadata(Collections.unmodifiableList(new ArrayList<>(predicates)));
      }
    }
  }
}
