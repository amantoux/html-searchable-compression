package com.plato.util.html;

/**
 * Created by Alan Mantoux.
 */
public enum Tag {
  HTML("html"),
  HEAD("head"),
  STYLE("style"),
  META("meta", true),
  BODY("body"),
  P("p"),
  STRONG("strong"),
  EM("em"),
  BR("br", true),
  A("a"),
  DIV("div"),
  SPAN("span"),
  TABLE("table"),
  TBODY("tbody"),
  THEAD("thead"),
  TR("tr"),
  TD("td"),
  H1("h1"),
  H2("h2"),
  H3("h3"),
  H4("h4"),
  H5("h5"),
  H6("h6"),
  HR("hr", true),
  IMG("img", true);


  String string;
  boolean isSelfClosing = false;

  Tag(String string) {
    this.string = string;
  }

  Tag(String string, boolean isSelfClosing) {
    this.string = string;
    this.isSelfClosing = isSelfClosing;
  }

  public static String getRegex() {
    StringBuilder sbRegex = new StringBuilder();
    sbRegex.append("</?(");
    for (Tag t : Tag.values()) {
      sbRegex.append(t.string).append("|");
    }
    sbRegex.deleteCharAt(sbRegex.length() - 1);

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

  public static Tag getInstance(String s) {
    for (Tag t : values()) {
      if (t.toString().equals(s))
        return t;
    }
    return null;
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

  @Override
  public String toString() {
    return string;
  }
}
