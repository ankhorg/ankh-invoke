package org.inksnow.ankhinvoke.nbt.api;

import java.util.List;

public interface NbtCollectionLike<E> extends NbtLike, List<E> {

  NbtCollectionLike<E> clone();
}
