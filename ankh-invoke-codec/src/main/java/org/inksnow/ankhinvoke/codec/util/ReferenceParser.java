package org.inksnow.ankhinvoke.codec.util;

import org.jetbrains.annotations.NotNull;

public class ReferenceParser {
  private ReferenceParser() {
    throw new UnsupportedOperationException();
  }

  public static @NotNull String @NotNull [] parse(@NotNull String reference) {
    String[] result = new String[3];
    int firstSplitIndex = reference.indexOf(';');
    if (firstSplitIndex == -1) {
      result[0] = reference;
      result[1] = "";
      result[2] = "";
    } else {
      result[0] = reference.substring(1, firstSplitIndex);
      int secondSplitIndex = reference.indexOf('(', firstSplitIndex);
      if (secondSplitIndex != -1) {
        // is method
        result[2] = reference.substring(secondSplitIndex);
      } else {
        // is field
        secondSplitIndex = reference.indexOf(':', firstSplitIndex);
        if (secondSplitIndex == -1) {
          throw new IllegalArgumentException("Illegal reference: " + reference);
        }
        result[2] = reference.substring(secondSplitIndex + 1);
      }
      result[1] = reference.substring(firstSplitIndex + 1, secondSplitIndex);
    }
    return result;
  }

  public static @NotNull String @NotNull [] parseField(@NotNull String reference) {
    String[] result = new String[3];
    int firstSplitIndex = reference.indexOf(';');
    if (firstSplitIndex == -1) {
      throw new IllegalArgumentException("Illegal field reference: " + reference);
    }
    result[0] = reference.substring(1, firstSplitIndex);
    // is field
    int secondSplitIndex = reference.indexOf(':', firstSplitIndex);
    if (secondSplitIndex == -1) {
      throw new IllegalArgumentException("Illegal field reference: " + reference);
    }
    result[2] = reference.substring(secondSplitIndex + 1);
    result[1] = reference.substring(firstSplitIndex + 1, secondSplitIndex);
    return result;
  }

  public static @NotNull String @NotNull [] parseMethod(@NotNull String reference) {
    String[] result = new String[3];
    int firstSplitIndex = reference.indexOf(';');
    if (firstSplitIndex == -1) {
      throw new IllegalArgumentException("Illegal method reference: " + reference);
    }
    result[0] = reference.substring(1, firstSplitIndex);
    int secondSplitIndex = reference.indexOf('(', firstSplitIndex);
    if (secondSplitIndex == -1) {
      throw new IllegalArgumentException("Illegal method reference: " + reference);
    }
    // is method
    result[2] = reference.substring(secondSplitIndex);
    result[1] = reference.substring(firstSplitIndex + 1, secondSplitIndex);
    return result;
  }
}
