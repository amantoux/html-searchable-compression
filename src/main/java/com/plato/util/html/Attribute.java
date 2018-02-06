package com.plato.util.html;

import static com.plato.util.html.HTMLSearchableCompression.ESCAPE;
import static com.plato.util.html.HTMLSearchableCompression.notBewteenQuotesRegex;

/**
 * Created by Alan Mantoux.
 */
public class Attribute implements StringSerializable {

  static final String ATTR_DELIMIT = ESCAPE + "attr";
  static String ATTR_REGEX =
      // attribute key should not contain '"' or '<' or '>' or ' ' or ';'
      "[^(\"|<|>|\\s|;)]+"
          // equals
          + "="
          // attribute value should not contain '"'
          + "\"[^\"]+\"";
  private String key;
  private String value;

  Attribute(String key, String value) {
    this.key = key;
    this.value = value;
  }

  static Attribute deserializeString(String a) {
    if (a == null)
      throw new NullPointerException("Input value is cannot be null");
    if ("".equals(a.trim()))
      throw new IllegalArgumentException("Attribute must have a key and a value");
    // notBetweenQuotesRegex to handle attribute with "=" in its value
    // e.g. : <meta content="text/html; charset=utf-8">
    String[] tmpAttr = a.split(notBewteenQuotesRegex("="));
    String[] tmpValue = tmpAttr[1].split("\"");
    if (tmpValue.length < 2)
      throw new IllegalArgumentException("Attribute value must be of form \"xxx\"");
    return new Attribute(tmpAttr[0], tmpValue[1]);
  }

  static void createAttribute(String sAttr, TagInstance t) {
    // notBetweenQuotesRegex to handle attribute with "=" in its value
    // e.g. : <meta content="text/html; charset=utf-8">
    String[] keyValue = sAttr.split(notBewteenQuotesRegex("="));
    String key = keyValue[0].trim();
    String value = keyValue[1].split("\"")[1].split("\"")[0].trim();
    t.addAttribute(new Attribute(key, value));
  }

  public String key() {
    return this.key;
  }

  public String value() {
    return this.value;
  }

  @Override
  public String serializeString() {
    return key + "=\"" + value + "\"";
  }

  @Override
  public String toString() {
    return key + "=\"" + value + "\"";
  }
}
