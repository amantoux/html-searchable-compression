package com.plato.util.html;

/**
 * Created by Alan Mantoux.
 */
public enum Tag {
  P("p"), STRONG("strong"), EM("em"), BR("br", true), A("a"), DIV("div"), SPAN("span"), H1(
    "h1"), H2("h2"), H3("h3");


  String string;
  boolean isSelfClosing = false;

  Tag(String string) {
    this.string = string;
  }

  Tag(String string, boolean isSelfClosing) {
    this.string = string;
    this.isSelfClosing = isSelfClosing;
  }

  public static Tag getInstance(String s) {
    for (Tag t : values()) {
      if (t.toString().equals(s))
        return t;
    }
    return null;
  }

  @Override
  public String toString() {
    return string;
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
    boolean hasAttribute = indexSpace >= 0;
    if (in.charAt(1) != '/') {
      return in.substring(1, hasAttribute ? indexSpace : in.indexOf('>'));
    }
    return in.substring(2, hasAttribute ? indexSpace : in.indexOf('>'));
  }

  public String closingString() {
    if (isSelfClosing)
      return openingString();
    return "</" + toString() + ">";
  }

  public String openingString() {
    return "<" + toString() + ">";
  }

  public static String getRegex() {
    StringBuilder sbRegex = new StringBuilder();
    sbRegex.append("</?(");
    for (Tag t : Tag.values()) {
      sbRegex.append(t.string).append("|");
    }
    sbRegex.deleteCharAt(sbRegex.length() - 1);

    // TODO: support any attribute
    // (^\")*
    sbRegex.append(")(\\s[^\"]+=\"[^\"]+\")*");
    sbRegex.append(">");
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
    sbRegex.append(")\\s?(").append(Attribute.ATTR_REGEX).append(")?>");
    return sbRegex.toString();
  }
}
