package org.inksnow.ankhinvoke.map.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.function.Consumer;

public class LineBufferPrintStream extends PrintStream {

  /**
   * logger where output of this log is sent
   */
  private final Consumer<String> consumer;
  /**
   * stream used for buffering lines
   */
  private final ByteArrayOutputStream bufOut;
  /**
   * record the last character written to this stream
   */
  private int last = -1;

  public LineBufferPrintStream(Consumer<String> consumer) {
    super(new ByteArrayOutputStream());
    bufOut = (ByteArrayOutputStream) super.out;
    this.consumer = consumer;
  }

  public void write(int b) {
    if ((last == '\r') && (b == '\n')) {
      last = -1;
      return;
    } else if ((b == '\n') || (b == '\r')) {
      try {
        /* write the converted bytes of the log message */
        consumer.accept(bufOut.toString("UTF-8"));
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      } finally {
        bufOut.reset();
      }
    } else {
      super.write(b);
    }
    last = b;
  }

  public void write(byte b[], int off, int len) {
    if (len < 0) {
      throw new ArrayIndexOutOfBoundsException(len);
    }
    for (int i = 0; i < len; i++) {
      write(b[off + i]);
    }
  }
}