package com.plato.util.html;

import static com.plato.util.html.HTMLSearchableCompression.ESCAPE;

/**
 * Created by Alan Mantoux.
 */
public class StyleAttribute implements StringSerializable {
  static final String STYLE = "style";
  static final String STYLE_REGEX = "style=\"(\\{)?"
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
  static final String STYLE_DELIMIT = ESCAPE + STYLE;
  private String key;
  private String value;

  StyleAttribute(String key, String value) {
    this.key = key;
    this.value = value;
  }

  static StyleAttribute deserializeString(String s) {
    if (s == null)
      throw new NullPointerException("Input value is cannot be null");
    if ("".equals(s.trim()))
      throw new IllegalArgumentException("Value of style attribute cannot be empty");
    String[] sProps = s.split(ESCAPE + ":");
    return new StyleAttribute(sProps[0], sProps[1]);
  }

  static void createStyleAttribute(String sAttr, TagInstance t) {
    String sMap = sAttr.split("\"(\\{)?")[1].split("(\\})?\"")[0];
    for (String s : sMap.split("\\;")) {
      String[] sKeyValue = s.split("\\:");
      t.addStyleAttribute(new StyleAttribute(sKeyValue[0].trim(), sKeyValue[1].trim()));
    }
  }


  String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String toString() {
    return key + ":" + value;
  }

  @Override
  public String serializeString() {
    return key + ESCAPE + ":" + value;
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
}
