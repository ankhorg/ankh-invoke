package org.inksnow.ankhinvoke.predicate;

import org.inksnow.ankhinvoke.PredicateService;
import org.inksnow.ankhinvoke.util.version.ComparableVersion;
import org.inksnow.ankhinvoke.util.version.VersionRange;
import org.jetbrains.annotations.NotNull;

public abstract class VersionMachine implements PredicateMachine {
  @Override
  public boolean test(@NotNull PredicateService service, @NotNull String expression) {
    ComparableVersion current = new ComparableVersion(value());
    VersionRange range = VersionRange.createFromVersionSpec(expression);
    return range.containsVersion(current);
  }

  protected abstract String value();
}
