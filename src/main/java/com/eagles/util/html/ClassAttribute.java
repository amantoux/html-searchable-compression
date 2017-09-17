package com.eagles.util.html;

/**
 * Created by Alan Mantoux.
 */
public class ClassAttribute {

  public static final String CLASS_REGEX = "class=\"([a-z0-9]|-|\\s)+\"";

  private String value;

  public ClassAttribute(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }
}
