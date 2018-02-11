package com.plato.util.html;

/**
 * Created by Alan Mantoux.
 */
public enum Tag {
  DOCTYPE("doctype", true),
  A("a"),
  ABBR("abbr"),
  ACRONYM("acronym"),
  ADDRESS("address"),
  APPLET("applet"),
  AREA("area", true),
  ARTICLE("article"),
  ASIDE("aside"),
  AUDIO("audio"),
  B("b"),
  BASE("base", true),
  BASEFONT("basefont", true),
  BDI("bdi"),
  BDO("bdo"),
  BIG("big"),
  BLOCKQUOTE("blockquote"),
  BODY("body"),
  BR("br", true),
  BUTTON("button"),
  CANEVAS("canevas"),
  CAPTION("caption"),
  CENTER("center"),
  CITE("cite"),
  CODE("code"),
  COL("col", true),
  COLGROUP("colgroup"),
  DATA("data"),
  DATALIST("datalist"),
  DD("dd"),
  DEL("del"),
  DETAILS("details"),
  DFN("dfn"),
  DIALOG("dialog"),
  DIR("dir"),
  DIV("div"),
  DL("dl"),
  DT("dt"),
  EM("em"),
  EMBED("embed", true),
  FIELDSET("fieldset"),
  FIGCAPTION("fieldcaption"),
  FIGURE("figure"),
  FONT("font"),
  FOOTER("footer"),
  FORM("form"),
  FRAME("frame"),
  FRAMESET("frameset"),
  H1("h1"),
  H2("h2"),
  H3("h3"),
  H4("h4"),
  H5("h5"),
  H6("h6"),
  HEAD("head"),
  HEADER("header"),
  HR("hr", true),
  HTML("html"),
  I("i"),
  IFRAME("iframe"),
  IMG("img", true),
  INPUT("input", true),
  INS("ins"),
  KDB("kdb"),
  LABEL("label"),
  LEGEND("legend"),
  LI("li"),
  LINK("link", true),
  MAIN("main"),
  MAP("map"),
  MARK("mark"),
  MENU("menu"),
  MENUITEM("menuitem"),
  META("meta", true),
  METER("meter"),
  NAV("nav"),
  NOFRAMES("noframes"),
  NOSCRIPT("noscript"),
  OBJECT("object"),
  OL("ol"),
  OPTGROUP("optgroup"),
  OPTION("option"),
  OUTPUT("output"),
  P("p"),
  PARAM("param"),
  PICTURE("picture"),
  PRE("pre"),
  PROGRESS("progress"),
  Q("q"),
  RP("rp"),
  RT("rt"),
  RUBY("ruby"),
  S("s"),
  SAMP("samp"),
  SCRIPT("script"),
  SECTION("section"),
  SELECT("select"),
  SMALL("small"),
  SOURCE("source", true),
  SPAN("span"),
  STRIKE("strike"),
  STRONG("strong"),
  STYLE("style"),
  SUB("sub"),
  SUMMARY("summary"),
  SUP("SUP"),
  TABLE("table"),
  TBODY("tbody"),
  TD("td"),
  TEMPLATE("template"),
  TEXTAREA("textarea"),
  TFOOT("tfoot"),
  TH("th"),
  THEAD("thead"),
  TIME("time"),
  TITLE("title"),
  TR("tr"),
  TRACK("track", true),
  TT("tt"),
  U("u"),
  UL("ul"),
  VAR("var"),
  VIDEO("video"),
  WBR("wbr", true);



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
    sbRegex.append(")(\\s[^\"]+=\"[^\"]*\")*");
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
