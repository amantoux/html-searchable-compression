package com.eagles.util.html;

/**
 * Created by Alan Mantoux.
 */
public enum TagName {
  P("p"), STRONG("strong"), EM("em"), BR("br", true), A("a");

  String string;
  boolean isSelfClosing = false;

  TagName(String string) {
    this.string = string;
  }

  TagName(String string, boolean isSelfClosing) {
    this.string = string;
    this.isSelfClosing = isSelfClosing;
  }

  public static TagName isTag(String input) {
    String sTagName = prepareString(input);
    for (TagName t: TagName.values()) {
      if (t.string.equals(sTagName))
        return t;
    }
    return null;
  }

  @Override
  public String toString() {
    return string;
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
    for (TagName t : TagName.values()) {
      sbRegex.append("</?").append(t.string).append('>').append('|');
    }
    sbRegex.deleteCharAt(sbRegex.length() - 1);
    return sbRegex.toString();
  }

  public static String getRegexClosing() {
    StringBuilder sbRegex = new StringBuilder();
    for (TagName t : TagName.values()) {
      sbRegex.append("</").append(t.string).append('>').append('|');
    }
    sbRegex.deleteCharAt(sbRegex.length() - 1);
    return sbRegex.toString();
  }

  public static String getRegexOpening() {
    StringBuilder sbRegex = new StringBuilder();
    for (TagName t : TagName.values()) {
      sbRegex.append('<').append(t.string).append('>').append('|');
    }
    sbRegex.deleteCharAt(sbRegex.length() - 1);
    return sbRegex.toString();
  }

  public String openingString() {
    return "<" + toString() + ">";
  }

  public String closingString() {
    if (isSelfClosing)
      return openingString();
    return "</" + toString() + ">";
  }

  public static void main(String[] args) {
  }
}
