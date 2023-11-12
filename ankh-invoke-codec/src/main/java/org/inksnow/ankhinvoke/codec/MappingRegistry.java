package org.inksnow.ankhinvoke.codec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class MappingRegistry {
  private final @NotNull List<@NotNull MappingSet> sets;

  private MappingRegistry(@NotNull List<@NotNull MappingSet> sets) {
    this.sets = sets;
  }

  public static @NotNull MappingRegistry load(@NotNull DataInputStream in) throws IOException {
    int setCount = in.readInt();
    List<MappingSet> sets = new ArrayList<>(setCount);
    for (int i = 0; i < setCount; i++) {
      sets.add(MappingSet.load(in));
    }
    return new MappingRegistry(Collections.unmodifiableList(sets));
  }

  public static @NotNull Builder builder() {
    return new Builder();
  }

  public @NotNull List<@NotNull MappingSet> sets() {
    return sets;
  }

  public void save(@NotNull DataOutputStream out) throws IOException {
    out.writeInt(sets.size());
    for (MappingSet set : sets) {
      set.save(out);
    }
  }

  public static final class Builder {
    private final @NotNull List<MappingSet.@NotNull Builder> sets = new ArrayList<>();

    public MappingSet.@NotNull Builder appendSet() {
      MappingSet.Builder subBuilder = new MappingSet.Builder(this);
      sets.add(subBuilder);
      return subBuilder;
    }

    public @NotNull MappingRegistry build() {
      List<MappingSet> sets = new ArrayList<>(this.sets.size());
      for (MappingSet.Builder set : this.sets) {
        sets.add(set.buildInternal());
      }
      return new MappingRegistry(Collections.unmodifiableList(sets));
    }
  }

  public static final class MappingSet {
    private final @NotNull String name;
    private final boolean isRequired;
    private final @NotNull List<@NotNull MappingEntry> entries;

    private MappingSet(@NotNull String name, boolean isRequired, @NotNull List<@NotNull MappingEntry> entries) {
      this.name = name;
      this.isRequired = isRequired;
      this.entries = entries;
    }

    public static @NotNull MappingSet load(@NotNull DataInputStream in) throws IOException {
      String name = in.readUTF();
      boolean isRequired = in.readBoolean();
      int entryCount = in.readInt();
      List<MappingEntry> entries = new ArrayList<>(entryCount);
      for (int i = 0; i < entryCount; i++) {
        entries.add(MappingEntry.load(in));
      }
      return new MappingSet(name, isRequired, Collections.unmodifiableList(entries));
    }

    public @NotNull String name() {
      return name;
    }

    public boolean isRequired() {
      return isRequired;
    }

    public @NotNull List<@NotNull MappingEntry> entries() {
      return entries;
    }

    public void save(@NotNull DataOutputStream out) throws IOException {
      out.writeUTF(name);
      out.writeBoolean(isRequired);
      out.writeInt(entries.size());
      for (MappingEntry entry : entries) {
        entry.save(out);
      }
    }

    public static final class Builder {
      private final MappingRegistry.@NotNull Builder registryBuilder;
      private final @NotNull List<MappingEntry.@NotNull Builder> entries = new ArrayList<>();
      private @Nullable String name;
      private boolean isRequired;

      private Builder(MappingRegistry.@NotNull Builder registryBuilder) {
        this.registryBuilder = registryBuilder;
      }

      public @NotNull Builder setName(@NotNull String name) {
        this.name = name;
        return this;
      }

      public @NotNull Builder setRequired() {
        isRequired = true;
        return this;
      }

      public @NotNull Builder setRequired(boolean required) {
        isRequired = required;
        return this;
      }

      public MappingEntry.@NotNull Builder appendEntry() {
        MappingEntry.Builder subBuilder = new MappingEntry.Builder(this);
        entries.add(subBuilder);
        return subBuilder;
      }

      public @NotNull Builder clearEntries() {
        entries.clear();
        return this;
      }

      public MappingRegistry.@NotNull Builder build() {
        return registryBuilder;
      }

      private @NotNull MappingSet buildInternal() {
        if (name == null) {
          throw new IllegalArgumentException("must special a name");
        }
        List<MappingEntry> entries = new ArrayList<>(this.entries.size());
        for (MappingEntry.Builder entry : this.entries) {
          entries.add(entry.buildInternal());
        }
        return new MappingSet(name, isRequired, entries);
      }
    }
  }

  public static final class MappingEntry {
    private final @NotNull String name;
    private final @NotNull List<@NotNull String> predicates;

    private MappingEntry(@NotNull String name, @NotNull List<@NotNull String> predicates) {
      this.name = name;
      this.predicates = predicates;
    }

    public static @NotNull MappingEntry load(@NotNull DataInputStream in) throws IOException {
      String name = in.readUTF();
      int predicateCount = in.readInt();
      List<String> predicates = new ArrayList<>(predicateCount);
      for (int i = 0; i < predicateCount; i++) {
        predicates.add(in.readUTF());
      }
      return new MappingEntry(name, Collections.unmodifiableList(predicates));
    }

    public @NotNull String name() {
      return name;
    }

    public @NotNull List<@NotNull String> predicates() {
      return predicates;
    }

    public void save(@NotNull DataOutputStream out) throws IOException {
      out.writeUTF(name);
      out.writeInt(predicates.size());
      for (String predicate : predicates) {
        out.writeUTF(predicate);
      }
    }

    public static final class Builder {
      private final MappingSet.@NotNull Builder mappingSetBuilder;
      private final @NotNull List<@NotNull String> predicates = new ArrayList<>();
      private @Nullable String name;

      private Builder(MappingSet.@NotNull Builder mappingSetBuilder) {
        this.mappingSetBuilder = mappingSetBuilder;
      }

      public @NotNull Builder setName(@NotNull String name) {
        this.name = name;
        return this;
      }

      public @NotNull Builder appendPredicate(@NotNull String predicate) {
        predicates.add(predicate);
        return this;
      }

      public @NotNull Builder appendPredicate(@NotNull List<String> predicateList) {
        predicates.addAll(predicateList);
        return this;
      }

      public @NotNull Builder appendPredicate(@NotNull String @NotNull... predicatesArray) {
        predicates.addAll(Arrays.asList(predicatesArray));
        return this;
      }

      public @NotNull Builder clearPredicates() {
        predicates.clear();
        return this;
      }

      public MappingSet.@NotNull Builder build() {
        return mappingSetBuilder;
      }

      private @NotNull MappingEntry buildInternal() {
        if (name == null) {
          throw new IllegalArgumentException("must special a name");
        }
        return new MappingEntry(name, Collections.unmodifiableList(new ArrayList<>(predicates)));
      }
    }
  }
}
