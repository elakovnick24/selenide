package com.codeborne.selenide.collections;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.ex.AttributesMismatch;
import com.codeborne.selenide.ex.ElementNotFound;
import com.codeborne.selenide.ex.ListSizeMismatch;
import com.codeborne.selenide.impl.CollectionSource;
import com.codeborne.selenide.impl.ElementCommunicator;
import org.openqa.selenium.WebElement;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;

import static com.codeborne.selenide.CheckResult.Verdict.REJECT;
import static com.codeborne.selenide.impl.Plugins.inject;
import static java.util.Collections.unmodifiableList;

@ParametersAreNonnullByDefault
public class Attributes extends CollectionCondition {
  private static final ElementCommunicator communicator = inject(ElementCommunicator.class);

  protected final List<String> expectedValues;
  protected final String attribute;

  public Attributes(String attribute, List<String> expectedValues) {
    if (expectedValues.isEmpty()) {
      throw new IllegalArgumentException("No expected values given for attribute " + attribute);
    }
    this.expectedValues = unmodifiableList(expectedValues);
    this.attribute = attribute;
  }

  @Override
  @Nonnull
  @CheckReturnValue
  public CheckResult check(Driver driver, List<WebElement> elements) {
    if (elements.size() != expectedValues.size()) {
      return new CheckResult(REJECT, elements.size());
    }
    List<String> actualAttributeValues = communicator.attributes(driver, elements, attribute);

    for (int i = 0; i < expectedValues.size(); i++) {
      String expectedValue = expectedValues.get(i);
      String actualValue = actualAttributeValues.get(i);

      if (!Objects.equals(actualValue, expectedValue)) {
        String message = String.format("Attribute \"%s\" values mismatch (#%s expected: \"%s\", actual: \"%s\")",
          attribute, i, expectedValue, actualValue);
        return CheckResult.rejected(message, actualAttributeValues);
      }
    }
    return CheckResult.accepted();
  }

  @Override
  public void fail(CollectionSource collection,
                   CheckResult lastCheckResult,
                   @Nullable Exception cause,
                   long timeoutMs) {
    if (lastCheckResult.actualValue() instanceof Integer actualSize) {
      if (actualSize == 0) {
        throw new ElementNotFound(collection, toString(), timeoutMs, cause);
      }
      else {
        throw new ListSizeMismatch("=", expectedValues.size(), actualSize, explanation, collection, cause, timeoutMs);
      }
    }

    List<String> actualAttributeValues = lastCheckResult.requireActualValue();

    if (actualAttributeValues.isEmpty()) {
      throw new ElementNotFound(collection, toString(), timeoutMs, cause);
    }

    String message = lastCheckResult.getMessageOrElse(() -> String.format("Attribute '%s' values mismatch", attribute));
    throw new AttributesMismatch(collection.driver(), message, collection, expectedValues,
      actualAttributeValues, explanation, timeoutMs, cause);
  }

  @Override
  public boolean missingElementSatisfiesCondition() {
    return false;
  }

  @Override
  public String toString() {
    return "Attribute: '" + attribute + "' values " + expectedValues;
  }
}
