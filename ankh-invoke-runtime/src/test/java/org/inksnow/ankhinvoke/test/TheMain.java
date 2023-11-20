package org.inksnow.ankhinvoke.test;

import org.inksnow.ankhinvoke.ref.RefTarget;

public class TheMain extends RefTarget {
  public static void main(String[] args) {
    System.out.println(new TheMain());
  }

  @Override
  public String refGetName() {
    return "TheMain";
  }
}
