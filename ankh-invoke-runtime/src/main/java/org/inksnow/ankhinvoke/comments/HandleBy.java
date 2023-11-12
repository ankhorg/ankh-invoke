package org.inksnow.ankhinvoke.comments;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(HandleBy.List.class)
public @interface HandleBy {
  String reference();

  String[] predicates() default {};

  boolean isInterface() default false;

  boolean useAccessor() default false;

  String[] metadata() default {};

  @Target({ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @interface List {
    HandleBy[] value();
  }
}
