package org.inksnow.ankhinvoke.codec.util;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;

public class TimeUtil {
  private static final ZoneId GMT_ZONE_ID = ZoneId.of("GMT");
  private static final DateTimeFormatter LOG_DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
      .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
      .appendLiteral('-')
      .appendValue(ChronoField.MONTH_OF_YEAR, 2)
      .appendLiteral('-')
      .appendValue(ChronoField.DAY_OF_MONTH, 2)
      .appendLiteral(' ')
      .appendValue(ChronoField.HOUR_OF_DAY, 2)
      .appendLiteral(':')
      .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
      .appendLiteral(':')
      .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
      .toFormatter();
  private TimeUtil() {
    throw new UnsupportedOperationException();
  }

  public static @NotNull String logTime() {
    return LocalDateTime.now(GMT_ZONE_ID).format(LOG_DATE_TIME_FORMATTER);
  }
}
