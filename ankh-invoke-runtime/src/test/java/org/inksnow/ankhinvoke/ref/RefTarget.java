package org.inksnow.ankhinvoke.ref;

import org.inksnow.ankhinvoke.comments.HandleBy;

@HandleBy(reference = "org/inksnow/ankhinvoke/target/TheTarget")
public class RefTarget {
  @HandleBy(reference = "Lorg/inksnow/ankhinvoke/target/TheTarget;<init>()V")
  public RefTarget() {
    throw new UnsupportedOperationException();
  }

  @HandleBy(reference = "Lorg/inksnow/ankhinvoke/target/TheTarget;getName()Ljava/lang/String;")
  public native String refGetName();
}
