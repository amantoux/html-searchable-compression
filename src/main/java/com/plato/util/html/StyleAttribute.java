package com.plato.util.html;

/**
 * Created by Alan Mantoux.
 */
public class StyleAttribute {
  public static final String STYLE_REGEX = "style=\"(\\{)?"
    // Open any style instruction
    + "("
    // Style key
    + "([a-z0-9]|-)*" + "\\:"
    // Style value
    + "([a-z0-9]|\\s|-|\\)|\\(|,|#)*" + "\\;"
    // Close any style instruction
    + ")*"
    // Clos option style attribute
    + "(\\})?\"";
  private String key;
  private String value;

  public StyleAttribute(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public int hashCode() {
    int result = key.hashCode();
    result = 31 * result + value.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    StyleAttribute that = (StyleAttribute) o;

    return key.equals(that.key) && value.equals(that.value);
  }

  public String toString() {
    return key + ":" + value;
  }
}
