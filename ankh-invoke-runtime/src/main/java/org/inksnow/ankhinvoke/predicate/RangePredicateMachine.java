package org.inksnow.ankhinvoke.predicate;

import org.inksnow.ankhinvoke.AnkhInvoke;
import org.inksnow.ankhinvoke.PredicateService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.StringTokenizer;

public abstract class RangePredicateMachine<T extends Comparable<T>> implements PredicateMachine {
  @Override
  public boolean test(@NotNull PredicateService service, @NotNull String expression) {
    if (expression.length() < 2) {
      throw new IllegalArgumentException("range predicate too short: " + expression);
    }
    char startChar = expression.charAt(0);
    char endChar = expression.charAt(expression.length() - 1);
    if (startChar == '{' && endChar == '}') {
      return testCollection(expression);
    } else if ((startChar == '(' || startChar == '[') && (endChar == ')' || endChar == ']')) {
      return testRange(expression);
    } else {
      throw new IllegalArgumentException("range predicate unknown type: " + expression);
    }
  }

  private boolean testCollection(@NotNull String expression) {
    boolean result = false;
    T value = value();
    StringTokenizer stringTokenizer = new StringTokenizer(expression.substring(1, expression.length() - 1), ",");
    while (stringTokenizer.hasMoreTokens()) {
      String token = stringTokenizer.nextToken();
      T tokenValue = parse(token);
      if (!result && value != null && tokenValue != null && compare(value, tokenValue) == 0) {
        if (AnkhInvoke.DEBUG) {
          return true;
        }
        result = true;
      }
    }
    return result;
  }

  private boolean testRange(@NotNull String expression) {
    boolean includeStart = (expression.charAt(0) == '[');
    boolean includeEnd = (expression.charAt(expression.length() - 1) == ']');

    int splitIndex = expression.indexOf(',', 1);
    if (splitIndex == -1) {
      throw new IllegalArgumentException("range predicate split not found: " + expression);
    }

    String startExpression = expression.substring(1, splitIndex);
    String endExpression = expression.substring(splitIndex + 1, expression.length() - 1);

    T value = value();
    T startValue = (startExpression.isEmpty() ? null : parse(startExpression));
    T endValue = (endExpression.isEmpty() ? null : parse(endExpression));

    if (value == null) {
      return false;
    }

    if (startValue != null) {
      int cmpResult = compare(startValue, value);
      if (cmpResult > 0 || (!includeStart && cmpResult == 0)) {
        return false;
      }
    }

    if (endValue != null) {
      int cmpResult = compare(endValue, value);
      return cmpResult >= 0 && (includeEnd || cmpResult != 0);
    }
    return true;
  }

  protected abstract @Nullable T value();

  protected abstract @Nullable T parse(@NotNull String expression);

  protected @Range(from = -1, to = 1) int compare(@NotNull T a, @NotNull T b) {
    return a.compareTo(b);
  }
}
