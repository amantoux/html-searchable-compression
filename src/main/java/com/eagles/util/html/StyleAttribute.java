package com.eagles.util.html;

/**
 * Created by Alan Mantoux.
 */
public class StyleAttribute {

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
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    StyleAttribute that = (StyleAttribute) o;

    return key.equals(that.key) && value.equals(that.value);
  }

  @Override
  public int hashCode() {
    int result = key.hashCode();
    result = 31 * result + value.hashCode();
    return result;
  }
}
