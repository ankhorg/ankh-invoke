package org.inksnow.ankhinvoke.codec;

import org.inksnow.ankhinvoke.codec.util.ReferenceParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

public final class BlobReference {
  private final @NotNull List<@NotNull String> superClasses;
  private final @NotNull List<@NotNull BlobHandle> handles;
  private final @NotNull Map<@NotNull String, @NotNull BlobEntry> fieldMap;
  private final @NotNull Map<@NotNull String, @NotNull BlobEntry> methodMap;

  private BlobReference(@NotNull List<@NotNull String> superClasses, @NotNull List<@NotNull BlobHandle> handles, @NotNull Map<@NotNull String, @NotNull BlobEntry> fieldMap, @NotNull Map<@NotNull String, @NotNull BlobEntry> methodMap) {
    this.superClasses = superClasses;
    this.handles = handles;
    this.fieldMap = fieldMap;
    this.methodMap = methodMap;
  }

  public @NotNull List<@NotNull String> superClasses() {
    return superClasses;
  }

  public @NotNull List<@NotNull BlobHandle> handles() {
    return handles;
  }

  public @NotNull Map<@NotNull String, @NotNull BlobEntry> fieldMap() {
    return fieldMap;
  }

  public @NotNull Map<@NotNull String, @NotNull BlobEntry> methodMap() {
    return methodMap;
  }

  public boolean isEmpty() {
    return handles.isEmpty() && fieldMap.isEmpty() && methodMap.isEmpty();
  }

  public void save(@NotNull DataOutputStream out) throws IOException {
    out.writeInt(superClasses.size());
    for (String superClass : superClasses) {
      out.writeUTF(superClass);
    }
    out.writeInt(handles.size());
    for (BlobHandle handle : handles) {
      handle.save(out);
    }
    out.writeInt(fieldMap.size());
    for (Map.Entry<String, BlobEntry> entry : fieldMap.entrySet()) {
      out.writeUTF(entry.getKey());
      entry.getValue().save(out);
    }
    out.writeInt(methodMap.size());
    for (Map.Entry<String, BlobEntry> entry : methodMap.entrySet()) {
      out.writeUTF(entry.getKey());
      entry.getValue().save(out);
    }
  }

  public static @NotNull BlobReference load(@NotNull DataInputStream in) throws IOException {
    int superClassCount = in.readInt();
    List<String> superClasses = new ArrayList<>(superClassCount);
    for (int i = 0; i < superClassCount; i++) {
      superClasses.add(in.readUTF());
    }

    int handleCount = in.readInt();
    List<BlobHandle> handles = new ArrayList<>(handleCount);
    for (int i = 0; i < handleCount; i++) {
      handles.add(BlobHandle.load(in));
    }

    int fieldMapSize = in.readInt();
    Map<String, BlobEntry> fieldMap = new HashMap<>(fieldMapSize);
    for (int i = 0; i < fieldMapSize; i++) {
      fieldMap.put(in.readUTF(), BlobEntry.load(in));
    }

    int methodMapSize = in.readInt();
    Map<String, BlobEntry> methodMap = new HashMap<>(methodMapSize);
    for (int i = 0; i < methodMapSize; i++) {
      methodMap.put(in.readUTF(), BlobEntry.load(in));
    }

    return new BlobReference(
        Collections.unmodifiableList(superClasses),
        Collections.unmodifiableList(handles),
        Collections.unmodifiableMap(fieldMap),
        Collections.unmodifiableMap(methodMap));
  }

  public static @NotNull Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private final @NotNull Set<@NotNull String> superClasses = new LinkedHashSet<>();
    private final @NotNull Set<@NotNull BlobHandle> handles = new LinkedHashSet<>();
    private final @NotNull Map<@NotNull String, @NotNull BlobEntry> fieldMap = new HashMap<>();
    private final @NotNull Map<@NotNull String, @NotNull BlobEntry> methodMap = new HashMap<>();

    public @NotNull Builder appendSuperClass(@Nullable String superClass) {
      if (superClass != null) {
        superClasses.add(superClass);
      }
      return this;
    }

    public @NotNull Builder appendHandle(@NotNull BlobHandle handle) {
      handles.add(handle);
      return this;
    }

    public @NotNull Builder appendField(@NotNull String name, @NotNull String describe, @NotNull BlobEntry entry) {
      fieldMap.put(name + ":" + describe, entry);
      return this;
    }

    public @NotNull Builder appendMethod(@NotNull String name, @NotNull String describe, @NotNull BlobEntry entry) {
      methodMap.put(name + describe, entry);
      return this;
    }

    public @NotNull BlobReference build() {
      return new BlobReference(
          Collections.unmodifiableList(new ArrayList<>(superClasses)),
          Collections.unmodifiableList(new ArrayList<>(handles)),
          Collections.unmodifiableMap(new HashMap<>(fieldMap)),
          Collections.unmodifiableMap(new HashMap<>(methodMap)));
    }
  }

  public static final class BlobEntry {
    private final @NotNull List<@NotNull BlobHandle> handles;

    private BlobEntry(@NotNull List<@NotNull BlobHandle> handles) {
      this.handles = handles;
    }

    public @NotNull List<@NotNull BlobHandle> handles() {
      return handles;
    }

    public boolean isEmpty() {
      return handles.isEmpty();
    }

    public void save(@NotNull DataOutputStream out) throws IOException {
      out.writeInt(handles.size());
      for (BlobHandle handle : handles) {
        handle.save(out);
      }
    }

    public static @NotNull BlobEntry load(@NotNull DataInputStream in) throws IOException {
      int size = in.readInt();
      List<BlobHandle> handles = new ArrayList<>(size);
      for (int i = 0; i < size; i++) {
        handles.add(BlobHandle.load(in));
      }
      return new BlobEntry(Collections.unmodifiableList(handles));
    }

    public static @NotNull Builder builder() {
      return new Builder();
    }

    public static final class Builder {
      private final @NotNull List<@NotNull BlobHandle> handles = new ArrayList<>();

      public @NotNull Builder appendHandle(@NotNull BlobHandle handle) {
        handles.add(handle);
        return this;
      }

      public @NotNull Builder clearHandle() {
        handles.clear();
        return this;
      }

      public @NotNull BlobEntry build() {
        return new BlobEntry(Collections.unmodifiableList(new ArrayList<>(handles)));
      }
    }
  }

  public static final class BlobHandle {
    private final @NotNull String owner;
    private final @NotNull String name;
    private final @NotNull String describe;
    private final @NotNull List<@NotNull String> predicates;
    private final boolean isInterface;
    private final boolean useAccessor;

    private BlobHandle(@NotNull String owner, @NotNull String name, @NotNull String describe, @NotNull List<@NotNull String> predicates, boolean isInterface, boolean useAccessor) {
      this.owner = owner;
      this.name = name;
      this.describe = describe;
      this.predicates = predicates;
      this.isInterface = isInterface;
      this.useAccessor = useAccessor;
    }

    public @NotNull  String owner() {
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

    public void save(@NotNull DataOutputStream out) throws IOException {
      out.writeUTF(owner);
      out.writeUTF(name);
      out.writeUTF(describe);
      out.writeInt(predicates.size());
      for (String predicate : predicates) {
        out.writeUTF(predicate);
      }
      out.writeBoolean(isInterface);
      out.writeBoolean(useAccessor);
    }

    public static @NotNull BlobHandle load(@NotNull DataInputStream in) throws IOException {
      String owner = in.readUTF();
      String name = in.readUTF();
      String describe = in.readUTF();
      int predicateCount = in.readInt();
      List<String> predicates = new ArrayList<>(predicateCount);
      for (int i = 0; i < predicateCount; i++) {
        predicates.add(in.readUTF());
      }
      boolean isInterface = in.readBoolean();
      boolean useAccessor = in.readBoolean();
      return new BlobHandle(owner, name, describe, Collections.unmodifiableList(predicates), isInterface, useAccessor);
    }

    public static @NotNull Builder builder() {
      return new Builder();
    }

    public static final class Builder {
      private @Nullable String owner;
      private @Nullable String name;
      private @Nullable String describe;
      private final @NotNull List<@NotNull String> predicates = new ArrayList<>();
      private boolean isInterface;
      private boolean useAccessor;

      public @NotNull Builder setOwner(@NotNull String owner) {
        this.owner = owner;
        return this;
      }

      public @NotNull Builder setName(@NotNull String name) {
        this.name = name;
        return this;
      }

      public @NotNull Builder setDescribe(@NotNull String describe) {
        this.describe = describe;
        return this;
      }

      public @NotNull Builder setReference(@NotNull String reference) {
        String[] parsedReference = ReferenceParser.parse(reference);
        setOwner(parsedReference[0]);
        setName(parsedReference[1]);
        setDescribe(parsedReference[2]);
        return this;
      }

      public @NotNull Builder appendPredicate(@NotNull String predicate) {
        predicates.add(predicate);
        return this;
      }

      public @NotNull Builder clearPredicate() {
        predicates.clear();
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

      public @NotNull BlobHandle build() {
        if(owner == null) {
          throw new IllegalStateException("required owner is null");
        }
        if(name == null) {
          throw new IllegalStateException("required name is null");
        }
        if(describe == null) {
          throw new IllegalStateException("required describe is null");
        }
        return new BlobHandle(owner, name, describe, Collections.unmodifiableList(new ArrayList<>(predicates)), isInterface, useAccessor);
      }
    }
  }
}
