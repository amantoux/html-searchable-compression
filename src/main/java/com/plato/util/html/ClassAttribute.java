package com.plato.util.html;

import static com.plato.util.html.HTMLSearchableCompression.ESCAPE;

/**
 * Created by Alan Mantoux.
 */
public class ClassAttribute implements Attribute {

  static final String CLASS_REGEX   = "class=\"([a-z0-9]|-|\\s)+\"";
  static final String CLASS_DELIMIT = ESCAPE + "class";

  private String value;

  ClassAttribute(String value) {
    this.value = value;
  }

  static ClassAttribute deserializeString(String c) {
    if (c == null)
      throw new NullPointerException("Input value is cannot be null");
    if ("".equals(c.trim()))
      throw new IllegalArgumentException("Value of class attribute cannot be empty");
    return new ClassAttribute(c);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    ClassAttribute that = (ClassAttribute) o;

    return value.equals(that.value);
  }

  @Override
  public String toString() {
    return value;
  }

  @Override
  public String serializeString() {
    return value;
  }

  String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
