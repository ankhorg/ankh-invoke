package org.inksnow.ankhinvoke.map.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class AiStringUtils {
  private AiStringUtils() {
    throw new UnsupportedOperationException();
  }

  @Contract("null, _, _ -> null; !null, _, _ -> !null")
  public static String substringAfterLastOrdinal(String str, @NotNull String separator, int ordinal) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    final int pos = org.apache.commons.lang3.StringUtils.lastOrdinalIndexOf(str, separator, ordinal);
    if (pos == -1 || pos == str.length() - 1) {
      return "";
    }
    return str.substring(pos + 1);
  }
}
