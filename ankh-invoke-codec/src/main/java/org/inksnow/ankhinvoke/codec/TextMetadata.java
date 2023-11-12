package org.inksnow.ankhinvoke.codec;

import org.inksnow.ankhinvoke.codec.util.NoCloseInputStream;
import org.inksnow.ankhinvoke.codec.util.NoCloseOutputStream;
import org.inksnow.ankhinvoke.codec.util.TimeUtil;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.util.*;

import static java.time.temporal.ChronoField.*;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;

public final class TextMetadata {
  private final @NotNull List<@NotNull String> lines;

  private TextMetadata(@NotNull List<@NotNull String> lines) {
    this.lines = lines;
  }

  public @NotNull List<@NotNull String> lines() {
    return lines;
  }

  public @NotNull Builder toBuilder() {
    Builder builder = new Builder();
    builder.lines.addAll(lines);
    return builder;
  }

  public void save(@NotNull OutputStream out) throws IOException {
    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new NoCloseOutputStream(out)))){
      for (String line : lines) {
        writer.write(line);
        writer.newLine();
      }
    }
  }

  public static @NotNull TextMetadata load(@NotNull InputStream in) throws IOException {
    List<String> lines = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new NoCloseInputStream(in)))) {
      String line = null;
      String nextLine;
      while ((nextLine = reader.readLine()) != null) {
        if(line != null) {
          lines.add(line);
        }
        line = nextLine;
      }
      if (line != null && !line.isEmpty()) {
        lines.add(line);
      }
    }
    return new TextMetadata(Collections.unmodifiableList(new ArrayList<>(lines)));
  }

  public static @NotNull Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private final @NotNull List<@NotNull String> previousLines = new ArrayList<>();
    private final @NotNull List<@NotNull String> lines = new ArrayList<>();

    public @NotNull Builder load(@NotNull InputStream in) throws IOException {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(new NoCloseInputStream(in)))) {
        String line = null;
        String nextLine;
        while ((nextLine = reader.readLine()) != null) {
          if(line != null) {
            previousLines.add(line);
          }
          line = nextLine;
        }
        if (line != null && !line.isEmpty()) {
          previousLines.add(line);
        }
      }
      return this;
    }

    public @NotNull Builder append(@NotNull String message) {
      lines.add("[" + TimeUtil.logTime() + "]\t" + message);
      return this;
    }

    public @NotNull Builder append(@NotNull String @NotNull ... messageArray) {
      String time = TimeUtil.logTime();
      for (String message : messageArray) {
        lines.add("[" + time + "]\t" + message);
      }
      return this;
    }

    public @NotNull Builder append(@NotNull Iterable<@NotNull String> messageIterable) {
      String time = TimeUtil.logTime();
      for (String message : messageIterable) {
        lines.add("[" + time + "]\t" + message);
      }
      return this;
    }

    public @NotNull Builder append(@NotNull Iterator<@NotNull String> messageIterator) {
      String time = TimeUtil.logTime();
      while (messageIterator.hasNext()) {
        lines.add("[" + time + "]\t" + messageIterator.next());
      }
      return this;
    }

    public @NotNull Builder save(@NotNull OutputStream out) throws IOException {
      build().save(out);
      return this;
    }

    public @NotNull TextMetadata build() {
      List<String> result = new ArrayList<>(previousLines.size() + lines.size());
      result.addAll(previousLines);
      result.addAll(lines);
      return new TextMetadata(Collections.unmodifiableList(result));
    }
  }
}
