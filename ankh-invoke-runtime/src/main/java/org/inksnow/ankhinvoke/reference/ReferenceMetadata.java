package org.inksnow.ankhinvoke.reference;

import org.inksnow.ankhinvoke.codec.BlobReference;
import org.inksnow.ankhinvoke.comments.HandleBy;
import org.inksnow.ankhinvoke.comments.InternalName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.*;
import java.util.function.Function;

public final class ReferenceMetadata {
  private static final ReferenceMetadata EMPTY_REFERENCE_METADATA = ReferenceMetadata.builder().build();

  private final @NotNull List<@InternalName @NotNull String> superClasses;
  private final @NotNull List<@NotNull Handle> handles;
  private final @NotNull Map<@NotNull String, @NotNull Entry> fieldMap;
  private final @NotNull Map<@NotNull String, @NotNull Entry> methodMap;

  private ReferenceMetadata(@NotNull List<@InternalName @NotNull String> superClasses, @NotNull List<@NotNull Handle> handles, @NotNull Map<@NotNull String, @NotNull Entry> fieldMap, @NotNull Map<@NotNull String, @NotNull Entry> methodMap) {
    this.superClasses = superClasses;
    this.handles = handles;
    this.fieldMap = fieldMap;
    this.methodMap = methodMap;
  }

  public static @NotNull Builder builder() {
    return new Builder();
  }

  public static @NotNull ReferenceMetadata empty() {
    return EMPTY_REFERENCE_METADATA;
  }

  public static @NotNull ReferenceMetadata fromBlob(@NotNull BlobReference blobReference) {
    Builder builder = ReferenceMetadata.builder();
    for (String superClass : blobReference.superClasses()) {
      builder.appendSuperClass(superClass);
    }
    for (BlobReference.BlobHandle blobHandle : blobReference.handles()) {
      builder.appendHandle(Handle.fromBlob(blobHandle));
    }
    for (Map.Entry<String, BlobReference.BlobEntry> entry : blobReference.fieldMap().entrySet()) {
      for (BlobReference.BlobHandle blobHandle : entry.getValue().handles()) {
        builder.appendField(entry.getKey(), Handle.fromBlob(blobHandle));
      }
    }
    for (Map.Entry<String, BlobReference.BlobEntry> entry : blobReference.methodMap().entrySet()) {
      for (BlobReference.BlobHandle blobHandle : entry.getValue().handles()) {
        builder.appendMethod(entry.getKey(), Handle.fromBlob(blobHandle));
      }
    }
    return builder.build();
  }

  public @NotNull List<@InternalName @NotNull String> superClasses() {
    return superClasses;
  }

  public @NotNull List<@NotNull Handle> handles() {
    return handles;
  }

  public @Nullable Entry field(String key) {
    return fieldMap.get(key);
  }

  public @Nullable Entry method(String key) {
    return methodMap.get(key);
  }

  public @NotNull Map<@NotNull String, @NotNull Entry> fieldMap() {
    return fieldMap;
  }

  public @NotNull Map<@NotNull String, @NotNull Entry> methodMap() {
    return methodMap;
  }

  public boolean isEmpty() {
    return handles.isEmpty() && fieldMap.isEmpty() && methodMap.isEmpty();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ReferenceMetadata metadata = (ReferenceMetadata) o;

    if (!superClasses.equals(metadata.superClasses)) return false;
    if (!handles.equals(metadata.handles)) return false;
    if (!fieldMap.equals(metadata.fieldMap)) return false;
    return methodMap.equals(metadata.methodMap);
  }

  @Override
  public int hashCode() {
    int result = superClasses.hashCode();
    result = 31 * result + handles.hashCode();
    result = 31 * result + fieldMap.hashCode();
    result = 31 * result + methodMap.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "ReferenceMetadata{" +
        "superClasses=" + superClasses +
        ", handles=" + handles +
        ", fieldMap=" + fieldMap +
        ", methodMap=" + methodMap +
        '}';
  }

  public static final class Handle {
    private final @NotNull String owner;
    private final @NotNull String name;
    private final @NotNull String describe;
    private final @NotNull List<@NotNull String> predicates;
    private final boolean isInterface;
    private final boolean useAccessor;

    private Handle(@NotNull String owner, @NotNull String name, @NotNull String describe, @NotNull List<@NotNull String> predicates, boolean isInterface, boolean useAccessor) {
      this.owner = owner;
      this.name = name;
      this.describe = describe;
      this.predicates = predicates;
      this.isInterface = isInterface;
      this.useAccessor = useAccessor;
    }

    public static @NotNull Handle fromBlob(BlobReference.@NotNull BlobHandle blobHandle) {
      return Handle.builder()
          .owner(blobHandle.owner())
          .name(blobHandle.name())
          .describe(blobHandle.describe())
          .appendPredicate(blobHandle.predicates())
          .isInterface(blobHandle.isInterface())
          .useAccessor(blobHandle.useAccessor())
          .build();
    }

    public static @NotNull Handle fromAnnotation(@NotNull HandleBy handleBy) {
      return Handle.builder()
          .reference(handleBy.reference())
          .appendPredicate(handleBy.predicates())
          .isInterface(handleBy.isInterface())
          .useAccessor(handleBy.useAccessor())
          .build();
    }

    @SuppressWarnings("unchecked") // never happened
    public static @NotNull Handle fromAnnotationNode(@NotNull AnnotationNode annotationNode) {
      Builder builder = Handle.builder();
      if (annotationNode.values == null || annotationNode.values.isEmpty()) {
        return builder.build();
      }
      String currentKey = null;
      for (Object value : annotationNode.values) {
        if (currentKey == null) {
          currentKey = (String) value;
          continue;
        }
        switch (currentKey) {
          case "reference": {
            builder.reference((String) value);
            currentKey = null;
            break;
          }
          case "predicates": {
            builder.appendPredicate((List<String>) value);
            currentKey = null;
            break;
          }
          case "isInterface": {
            builder.isInterface((Boolean) value);
            currentKey = null;
            break;
          }
          case "useAccessor": {
            builder.useAccessor((Boolean) value);
            currentKey = null;
            break;
          }
          default: {
            throw new IllegalStateException("unknown key: " + currentKey);
          }
        }
      }
      return builder.build();
    }

    public static @NotNull Builder builder() {
      return new Builder();
    }

    public @NotNull String owner() {
      return owner;
    }

    public @NotNull String name() {
      return name;
    }

    public @NotNull String describe() {
      return describe;
    }

    public @NotNull List<@NotNull String> predicates() {
      return predicates;
    }

    public boolean isInterface() {
      return isInterface;
    }

    public boolean useAccessor() {
      return useAccessor;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Handle handle = (Handle) o;

      if (isInterface != handle.isInterface) return false;
      if (useAccessor != handle.useAccessor) return false;
      if (!owner.equals(handle.owner)) return false;
      if (!name.equals(handle.name)) return false;
      if (!describe.equals(handle.describe)) return false;
      return predicates.equals(handle.predicates);
    }

    @Override
    public int hashCode() {
      int result = owner.hashCode();
      result = 31 * result + name.hashCode();
      result = 31 * result + describe.hashCode();
      result = 31 * result + predicates.hashCode();
      result = 31 * result + (isInterface ? 1 : 0);
      result = 31 * result + (useAccessor ? 1 : 0);
      return result;
    }

    @Override
    public String toString() {
      return "Handle{" +
          "owner='" + owner + '\'' +
          ", name='" + name + '\'' +
          ", describe='" + describe + '\'' +
          ", predicateList=" + predicates +
          ", isInterface=" + isInterface +
          ", useAccessor=" + useAccessor +
          '}';
    }

    public static final class Builder {
      private final @NotNull List<@NotNull String> predicates = new ArrayList<>();
      private @Nullable String owner;
      private @Nullable String name;
      private @Nullable String describe;
      private boolean isInterface;
      private boolean useAccessor;

      public @NotNull Builder owner(@NotNull String owner) {
        this.owner = owner;
        return this;
      }

      public @NotNull Builder name(@NotNull String name) {
        this.name = name;
        return this;
      }

      public @NotNull Builder describe(@NotNull String describe) {
        this.describe = describe;
        return this;
      }

      public @NotNull Builder reference(@NotNull String reference) {
        int firstSplitIndex = reference.indexOf(';');
        if (firstSplitIndex == -1) {
          owner = reference;
          name = "";
          describe = "";
        } else {
          owner = reference.substring(1, firstSplitIndex);
          int secondSplitIndex = reference.indexOf('(', firstSplitIndex);
          if (secondSplitIndex != -1) {
            // is method
            describe = reference.substring(secondSplitIndex);
          } else {
            // is field
            secondSplitIndex = reference.indexOf(':', firstSplitIndex);
            if (secondSplitIndex == -1) {
              throw new IllegalArgumentException("Illegal reference: " + reference);
            }
            describe = reference.substring(secondSplitIndex + 1);
          }
          name = reference.substring(firstSplitIndex + 1, secondSplitIndex);
        }
        return this;
      }

      public @NotNull Builder appendPredicate(@NotNull String predicate) {
        this.predicates.add(predicate);
        return this;
      }

      public @NotNull Builder appendPredicate(@NotNull Collection<@NotNull String> predicates) {
        this.predicates.addAll(predicates);
        return this;
      }

      public @NotNull Builder appendPredicate(@NotNull String @NotNull ... predicates) {
        this.predicates.addAll(Arrays.asList(predicates));
        return this;
      }

      public @NotNull Builder clearPredicate() {
        this.predicates.clear();
        return this;
      }

      public @NotNull Builder isInterface() {
        this.isInterface = true;
        return this;
      }

      public @NotNull Builder isInterface(boolean isInterface) {
        this.isInterface = isInterface;
        return this;
      }

      public @NotNull Builder useAccessor() {
        this.useAccessor = true;
        return this;
      }

      public @NotNull Builder useAccessor(boolean useAccessor) {
        this.useAccessor = useAccessor;
        return this;
      }

      public Handle build() {
        if (owner == null) {
          throw new IllegalArgumentException("owner must be special");
        }

        if (name == null) {
          throw new IllegalArgumentException("name must be special");
        }

        if (describe == null) {
          throw new IllegalArgumentException("describe must be special");
        }

        return new Handle(owner, name, describe, Collections.unmodifiableList(new ArrayList<>(predicates)), isInterface, useAccessor);
      }
    }
  }

  public static final class Entry {
    private final @NotNull List<@NotNull Handle> handles;

    public Entry(@NotNull List<@NotNull Handle> handles) {
      this.handles = handles;
    }

    public @NotNull List<@NotNull Handle> handles() {
      return handles;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Entry method = (Entry) o;

      return handles.equals(method.handles);
    }

    @Override
    public int hashCode() {
      return handles.hashCode();
    }

    @Override
    public @NotNull String toString() {
      return "ReferenceMetadata.Entry{" +
          "handles=" + handles +
          '}';
    }

    public static final class Builder {
      private final @NotNull List<@NotNull Handle> handles = new LinkedList<>();

      public @NotNull Builder appendHandle(@NotNull Handle handle) {
        if (!handles.contains(handle)) {
          handles.add(handle);
        }
        return this;
      }

      public @NotNull Entry build() {
        return new Entry(Collections.unmodifiableList(new ArrayList<>(handles)));
      }
    }
  }

  public static final class Builder {
    private static final @NotNull Function<@NotNull String, Entry.@NotNull Builder> entryBuilderMappingFunction =
        (it) -> new Entry.Builder();
    private final @NotNull Set<@InternalName @NotNull String> superClasses = new LinkedHashSet<>();
    private final @NotNull Set<@NotNull Handle> handles = new LinkedHashSet<>();
    private final @NotNull Map<@NotNull String, Entry.@NotNull Builder> fieldBuilderMap = new LinkedHashMap<>();
    private final @NotNull Map<@NotNull String, Entry.@NotNull Builder> methodBuilderMap = new LinkedHashMap<>();

    private static @NotNull Map<@NotNull String, @NotNull Entry> buildUnmodifiableMap(@NotNull Map<@NotNull String, Entry.@NotNull Builder> builderMap) {
      Map<String, Entry> map = new LinkedHashMap<>(builderMap.size());
      for (Map.Entry<String, Entry.Builder> builderEntry : builderMap.entrySet()) {
        map.put(builderEntry.getKey(), builderEntry.getValue().build());
      }
      return Collections.unmodifiableMap(map);
    }

    public @NotNull Builder appendSuperClass(@InternalName @Nullable String superClass) {
      if (superClass != null) {
        superClasses.add(superClass);
      }
      return this;
    }

    public @NotNull Builder appendSuperClass(@InternalName @Nullable List<@NotNull String> superClasses) {
      if (superClasses != null) {
        this.superClasses.addAll(superClasses);
      }
      return this;
    }


    public @NotNull Builder appendHandle(@NotNull Handle handle) {
      handles.add(handle);
      return this;
    }

    public @NotNull Builder appendField(@NotNull String key, @NotNull Handle handle) {
      Entry.Builder entryBuilder = fieldBuilderMap.computeIfAbsent(key, entryBuilderMappingFunction);
      entryBuilder.appendHandle(handle);
      return this;
    }

    public @NotNull Builder appendMethod(@NotNull String key, @NotNull Handle handle) {
      Entry.Builder entryBuilder = methodBuilderMap.computeIfAbsent(key, entryBuilderMappingFunction);
      entryBuilder.appendHandle(handle);
      return this;
    }

    public @NotNull Builder append(@NotNull ReferenceMetadata metadata) {
      return append(metadata, true);
    }

    public @NotNull Builder append(@NotNull ReferenceMetadata metadata, boolean withRoot) {
      if (withRoot) {
        for (String superClass : metadata.superClasses) {
          appendSuperClass(superClass);
        }
        for (Handle handle : metadata.handles) {
          appendHandle(handle);
        }
      }
      for (Map.Entry<String, Entry> entry : metadata.fieldMap.entrySet()) {
        for (Handle handle : entry.getValue().handles()) {
          appendField(entry.getKey(), handle);
        }
      }
      for (Map.Entry<String, Entry> entry : metadata.methodMap.entrySet()) {
        for (Handle handle : entry.getValue().handles()) {
          appendMethod(entry.getKey(), handle);
        }
      }
      return this;
    }

    public @NotNull ReferenceMetadata build() {
      List<String> superClasses = Collections.unmodifiableList(new ArrayList<>(this.superClasses));
      List<Handle> handles = Collections.unmodifiableList(new ArrayList<>(this.handles));
      Map<String, Entry> fieldMap = buildUnmodifiableMap(fieldBuilderMap);
      Map<String, Entry> methodMap = buildUnmodifiableMap(methodBuilderMap);

      return new ReferenceMetadata(superClasses, handles, fieldMap, methodMap);
    }
  }
}
