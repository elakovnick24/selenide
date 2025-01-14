package com.codeborne.selenide.commands;

import com.codeborne.selenide.Command;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.TypeOptions;
import com.codeborne.selenide.impl.WebElementSource;
import org.openqa.selenium.WebElement;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;

import static com.codeborne.selenide.Stopwatch.sleepAtLeast;
import static com.codeborne.selenide.commands.Util.firstOf;
import static com.codeborne.selenide.impl.Plugins.inject;
import static java.util.Objects.requireNonNull;

@ParametersAreNonnullByDefault
public class Type implements Command<SelenideElement> {
  private final Clear clear;

  public Type() {
    this(inject(Clear.class));
  }

  protected Type(Clear clear) {
    this.clear = clear;
  }

  @Nullable
  @Override
  public SelenideElement execute(SelenideElement proxy, WebElementSource locator, Object[] args) throws IOException {
    TypeOptions typeOptions = extractOptions(requireNonNull(args));
    clearField(proxy, locator, typeOptions);

    WebElement element = locator.findAndAssertElementIsEditable();
    typeIntoField(element, typeOptions);
    return proxy;
  }

  private TypeOptions extractOptions(Object[] args) {
    if (args[0] instanceof TypeOptions options) {
      return options;
    } else {
      return TypeOptions.text(firstOf(args));
    }
  }

  private void typeIntoField(WebElement element, TypeOptions typeOptions) {
    for (char character : typeOptions.textToType().toCharArray()) {
      element.sendKeys(String.valueOf(character));
      sleepAtLeast(typeOptions.timeDelay().toMillis());
    }
  }

  private void clearField(SelenideElement proxy, WebElementSource locator, TypeOptions typeOptions) {
    if (typeOptions.shouldClearFieldBeforeTyping()) {
      if (!typeOptions.textToType().isEmpty()) {
        clear.clear(locator.driver(), proxy);
      } else {
        clear.clearAndTrigger(locator.driver(), proxy);
      }
    }
  }
}
