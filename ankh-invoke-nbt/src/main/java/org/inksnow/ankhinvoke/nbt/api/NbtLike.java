package org.inksnow.ankhinvoke.nbt.api;

public interface NbtLike {
  byte getId();

  String getAsString();

  NbtLike clone();
}
