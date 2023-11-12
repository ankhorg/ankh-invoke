package org.inksnow.ankhinvoke.nbt.api;

import org.inksnow.ankhinvoke.nbt.Nbt;

public interface NbtListLike extends NbtCollectionLike<Nbt<?>> {
  NbtListLike clone();
}
