package com.eagles.util.html;

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

  public static void main(String[] args) {
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

  public static String getRegex() {
    StringBuilder sbRegex = new StringBuilder();
    String spaceStyleOpt = "(" + StyleAttribute.STYLE_REGEX + ")";
    String spaceClassOpt = "(" + ClassAttribute.CLASS_REGEX + ")";

    sbRegex.append("</?(");
    for (Tag t : Tag.values()) {
      sbRegex.append(t.string).append("|");
    }
    sbRegex.deleteCharAt(sbRegex.length() - 1);
    sbRegex.append(")(\\s(class|style)=\"[^\"]+\")*");
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
