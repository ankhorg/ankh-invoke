package org.inksnow.ankhinvoke.nbt.api;

public interface NbtNumericLike extends NbtLike {
  long getAsLong();

  int getAsInt();

  short getAsShort();

  byte getAsByte();

  double getAsDouble();

  float getAsFloat();

  Number getAsNumber();

  NbtNumericLike clone();
}
