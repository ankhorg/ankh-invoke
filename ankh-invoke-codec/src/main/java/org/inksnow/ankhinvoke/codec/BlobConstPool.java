package org.inksnow.ankhinvoke.codec;

import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;

public final class BlobConstPool {
  private static final BiFunction<String, Integer, Integer> ADD_ONE = (it, i) -> (i == null ? 0 : i) + 1;
  private final @NotNull List<@NotNull String> id2str;
  private final @NotNull Map<@NotNull String, @NotNull Integer> str2id;

  private BlobConstPool(@NotNull List<@NotNull String> id2str, @NotNull Map<@NotNull String, @NotNull Integer> str2id) {
    this.id2str = id2str;
    this.str2id = str2id;
  }

  public @NotNull String get(int key) {
    return id2str.get(key);
  }

  public int getKey(@NotNull String value) {
    return str2id.get(value);
  }

  public static @NotNull BlobConstPool load(@NotNull DataInputStream in) throws IOException {
    int size = in.readInt();
    String[] id2str = new String[size];
    Map<String, Integer> str2id = new HashMap<>(size);
    for (int i = 0; i < size; i++) {
      String value = in.readUTF();
      id2str[i] = value;
      str2id.put(value, 1);
    }
    return new BlobConstPool(Collections.unmodifiableList(Arrays.asList(id2str)), str2id);
  }

  public void save(@NotNull DataOutputStream out) throws IOException {
    out.writeInt(id2str.size());
    for (String s : id2str) {
      out.writeUTF(s);
    }
  }

  public static @NotNull Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private final @NotNull Map<@NotNull String, @NotNull Integer> poolMap = new HashMap<>();

    public void append(@NotNull String s0) {
      poolMap.compute(s0, ADD_ONE);
    }

    public void append(@NotNull String s0, @NotNull String s1) {
      poolMap.compute(s0, ADD_ONE);
      poolMap.compute(s1, ADD_ONE);
    }

    public void append(@NotNull String s0, @NotNull String s1, @NotNull String s2) {
      poolMap.compute(s0, ADD_ONE);
      poolMap.compute(s1, ADD_ONE);
      poolMap.compute(s2, ADD_ONE);
    }

    public void append(@NotNull String ... s) {
      for (String str : s) {
        poolMap.compute(str, ADD_ONE);
      }
    }

    public @NotNull BlobConstPool build() {
      String[] id2str = poolMap.entrySet()
          .stream()
          .sorted(Map.Entry.comparingByValue())
          .map(Map.Entry::getKey)
          .toArray(String[]::new);
      Map<String, Integer> str2id = new HashMap<>(id2str.length);
      for (int i = 0; i < id2str.length; i++) {
        str2id.put(id2str[i], i);
      }
      return new BlobConstPool(Collections.unmodifiableList(Arrays.asList(id2str)), str2id);
    }
  }
}
