package com.eagles.util.html;

/**
 * Created by Alan Mantoux.
 */
public enum Tag {
  P("p"), STRONG("strong"), EM("em"), BR("br", true), A("a");


  String string;
  boolean isSelfClosing = false;

  Tag(String string) {
    this.string = string;
  }

  Tag(String string, boolean isSelfClosing) {
    this.string = string;
    this.isSelfClosing = isSelfClosing;
  }

  public static Tag isTag(String input) {
    String sTagName = prepareString(input);
    for (Tag t : Tag.values()) {
      if (t.string.equals(sTagName))
        return t;
    }
    return null;
  }

  public static String prepareString(String in) {
    int indexSpace = in.indexOf(' ');
    boolean hasAtribute = indexSpace >= 0;
    if (in.charAt(1) != '/') {
      return in.substring(1, hasAtribute ? indexSpace : in.indexOf('>'));
    }
    return in.substring(2, hasAtribute ? indexSpace : in.indexOf('>'));
  }

  public static String getRegex() {
    StringBuilder sbRegex = new StringBuilder();
    sbRegex.append("</?(");
    for (Tag t : Tag.values()) {
      sbRegex.append(t.string).append("|");
    }
    sbRegex.deleteCharAt(sbRegex.length() - 1);
    sbRegex.append(")\\s?(").append(StyleAttribute.ATTR_REGEX).append(")?>");
    return sbRegex.toString();
  }

  public static String getRegexClosing() {
    StringBuilder sbRegex = new StringBuilder();
    for (Tag t : Tag.values()) {
      sbRegex.append("</").append(t.string).append('>').append('|');
    }
    sbRegex.deleteCharAt(sbRegex.length() - 1);
    return sbRegex.toString();
  }

  public static String getRegexOpening() {
    StringBuilder sbRegex = new StringBuilder();
    sbRegex.append("<(");
    for (Tag t : Tag.values()) {
      sbRegex.append(t.string).append("|");
    }
    sbRegex.deleteCharAt(sbRegex.length() - 1);
    sbRegex.append(")\\s?(").append(StyleAttribute.ATTR_REGEX).append(")?>");
    return sbRegex.toString();
  }

  public String closingString() {
    if (isSelfClosing)
      return openingString();
    return "</" + toString() + ">";
  }

  public String openingString() {
    return "<" + toString() + ">";
  }

  @Override
  public String toString() {
    return string;
  }

  public static void main(String[] args) {
  }
}
