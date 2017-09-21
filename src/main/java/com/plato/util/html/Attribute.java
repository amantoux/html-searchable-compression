package com.plato.util.html;

/**
 * Created by Alan Mantoux.
 */
public class Attribute {

  public static final String ATTR_REGEX =
    // attribute key
    "([a-z0-9]|-)*"
      // equals
      + "="
      // regular attribute (class or other)
      + "\"(.)*\"";

  private String key;
  private String value;

  public Attribute(String key, String value) {
    this.key = key;
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

    Attribute that = (Attribute) o;

    return key.equals(that.key) && value.equals(that.value);
  }

  public String toString() {
    return key + "=" + value;
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
}
