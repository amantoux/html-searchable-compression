package com.plato.util.html;

import java.util.LinkedList;
import java.util.List;

import static com.plato.util.html.Attribute.ATTR_DELIMIT;
import static com.plato.util.html.ClassAttribute.CLASS_DELIMIT;
import static com.plato.util.html.HTMLSearchableCompression.ESCAPE;
import static com.plato.util.html.HTMLSearchableCompression.notBewteenQuotesRegex;
import static com.plato.util.html.StyleAttribute.STYLE_DELIMIT;

/**
 * Created by Alan Mantoux.
 */
public class TagInstance {

  static final         String TAG_DELIMIT              = ESCAPE + "tag";
  private static final String OUT_OF_RANGE             =
    "rangeFrom and rangeTo must both be greater or equal than 0";
  private static final String SEMICOLON_NOT_BTW_QUOTES = notBewteenQuotesRegex(";");
  private static final String UNRECOGNIZED             = " is not a recognized tag";
  private Tag                  tag;
  private List<StyleAttribute> styleAttributes;
  private List<ClassAttribute> classAttributes;
  private List<Attribute>      otherAttributes;
  private int                  rangeFrom;
  private int                  rangeTo;

  public TagInstance(String sTagName, int rangeFrom, int rangeTo) {

    if (sTagName == null || sTagName.trim().isEmpty())
      throw new NullPointerException("TagInstance string cannot be null or empty");

    if (rangeFrom < 0 || rangeTo < 0)
      throw new IllegalArgumentException(OUT_OF_RANGE);

    Tag locTag = Tag.isTag(sTagName);
    if (locTag == null)
      throw new IllegalArgumentException(sTagName + UNRECOGNIZED);

    this.tag = locTag;
    this.rangeFrom = rangeFrom;
    if (tag.isSelfClosing) {
      this.rangeTo = this.rangeFrom;
      this.styleAttributes = null;
      return;
    }
    this.rangeTo = rangeTo;
    this.styleAttributes = new LinkedList<>();
    this.classAttributes = new LinkedList<>();
    this.otherAttributes = new LinkedList<>();
  }

  private TagInstance(Tag tag, int rangeFrom, int rangeTo) {
    if (rangeFrom < 0 || rangeTo < 0)
      throw new IllegalArgumentException(OUT_OF_RANGE);
    this.tag = tag;
    this.rangeFrom = rangeFrom;
    this.rangeTo = rangeTo;
    this.styleAttributes = new LinkedList<>();
    this.classAttributes = new LinkedList<>();
    this.otherAttributes = new LinkedList<>();
  }

  // Only for self closing tags
  private TagInstance(Tag tag, int rangeFrom) {
    if (rangeFrom < 0)
      throw new IllegalArgumentException(OUT_OF_RANGE);
    this.tag = tag;
    this.tag.isSelfClosing = true;
    this.rangeFrom = rangeFrom;
    this.rangeTo = rangeFrom;
    this.styleAttributes = new LinkedList<>();
    this.classAttributes = new LinkedList<>();
    this.otherAttributes = new LinkedList<>();
  }

  public TagInstance(String sTagName, int rangeFrom) {

    if (rangeFrom < 0)
      throw new IllegalArgumentException(OUT_OF_RANGE);

    Tag locTag = Tag.isTag(sTagName);
    if (locTag == null)
      throw new IllegalArgumentException(sTagName + UNRECOGNIZED);

    this.tag = locTag;
    this.rangeFrom = rangeFrom;
    if (locTag.isSelfClosing)
      this.rangeTo = rangeFrom;
    this.styleAttributes = new LinkedList<>();
    this.classAttributes = new LinkedList<>();
    this.otherAttributes = new LinkedList<>();
  }

  List<StyleAttribute> getStyleAttributes() {
    return styleAttributes;
  }

  List<ClassAttribute> getClassAttributes() {
    return classAttributes;
  }

  List<Attribute> getOtherAttributes() {
    return otherAttributes;
  }

  void setRangeTo(int to) {
    rangeTo = to;
  }

  static TagInstance deserializeString(String in, boolean selfClosing) {
    // Isolate tag props
    // it will always start with style then class
    String regexStyleOrClass =
      "(" + STYLE_DELIMIT + ")|(" + CLASS_DELIMIT + ")|(" + ATTR_DELIMIT + ")";
    String[] sTemp = in.split(regexStyleOrClass);
    String[] sTagProps = sTemp[0].split(";");
    TagInstance t;
    if (selfClosing) {
      t = new TagInstance(Tag.getInstance(sTagProps[0]), Integer.parseInt(sTagProps[1]));
    } else {
      t = new TagInstance(Tag.getInstance(sTagProps[0]), Integer.parseInt(sTagProps[1]),
        Integer.parseInt(sTagProps[2]));
    }
    // If no attribute, return
    if (sTemp.length < 2 || "".equals(sTemp[1].trim()))
      return t;

    boolean hasStyle = in.contains(STYLE_DELIMIT);
    boolean hasClass = in.contains(CLASS_DELIMIT);
    boolean hasOtherAttr = in.contains(ATTR_DELIMIT);
    int indexOfStyle = 1;
    int indexOfClass = hasStyle ? indexOfStyle + 1 : indexOfStyle;
    int indexOfOtherAttr = hasClass ? indexOfClass + 1 : indexOfClass;
    if (hasStyle) {
      // parse style
      String[] sStyles = sTemp[indexOfStyle].split(";");
      for (String s : sStyles) {
        t.addStyleAttribute(StyleAttribute.deserializeString(s));
      }
    }

    if (hasClass) {
      // parse class
      String[] sClass = sTemp[indexOfClass].split(";");
      for (String c : sClass) {
        t.addClassAttribute(ClassAttribute.deserializeString(c));
      }
    }

    if (hasOtherAttr) {
      String[] sAttr = sTemp[indexOfOtherAttr].split(SEMICOLON_NOT_BTW_QUOTES);
      for (String a : sAttr) {
        t.addAttribute(Attribute.deserializeString(a));
      }
    }

    return t;
  }

  public void addStyleAttribute(StyleAttribute s) {
    styleAttributes.add(s);
  }

  public void addAttribute(Attribute attribute) {
    otherAttributes.add(attribute);
  }

  void addClassAttribute(ClassAttribute c) {
    classAttributes.add(c);
  }

  String openingString() {
    StringBuilder openingString = new StringBuilder("<" + tagName().toString());

    // set class attributes
    if (!classAttributes.isEmpty()) {
      openingString.append(" ");
      openingString.append("class=\"");
      for (ClassAttribute c : classAttributes)
        openingString.append(c.toString()).append(" ");
      openingString.deleteCharAt(openingString.length() - 1);
      openingString.append("\"");
    }

    // set style attributes
    if (!styleAttributes.isEmpty()) {
      openingString.append(" ");
      openingString.append("style=\"");
      for (StyleAttribute s : styleAttributes)
        openingString.append(s.toString()).append(";");
      openingString.append("\"");
    }

    for (Attribute attr : otherAttributes) {
      openingString.append(" ");
      openingString.append(attr.key());
      openingString.append("=\"");
      openingString.append(attr.value());
      openingString.append("\"");
    }

    return openingString.append(">").toString();
  }

  Tag tagName() {
    return tag;
  }

  String closingString() {
    return tagName().closingString();
  }

  String serializeString() {
    StringBuilder s = new StringBuilder();
    s.append(tag.toString()).append(";").append(from());
    if (!tag.isSelfClosing)
      s.append(";").append(to());
    addAttributesToStringBuilder(s, styleAttributes, STYLE_DELIMIT);
    addAttributesToStringBuilder(s, classAttributes, CLASS_DELIMIT);
    addAttributesToStringBuilder(s, otherAttributes, ATTR_DELIMIT);
    return s.toString();
  }

  int from() {
    return rangeFrom;
  }

  int to() {
    return rangeTo;
  }

  private void addAttributesToStringBuilder(StringBuilder s,
                                            List<? extends StringSerializable> attributes,
                                            String attrTag) {
    if (!attributes.isEmpty()) {
      s.append(attrTag);
      for (StringSerializable a : attributes) {
        s.append(a.serializeString()).append(";");
      }
      s.deleteCharAt(s.length() - 1);
    }
  }

  @Override
  public int hashCode() {
    int result = tag.hashCode();
    result = 31 * result + styleAttributes.hashCode();
    result = 31 * result + classAttributes.hashCode();
    result = 31 * result + rangeFrom;
    result = 31 * result + rangeTo;
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    TagInstance that = (TagInstance) o;

    if (rangeFrom != that.rangeFrom)
      return false;
    if (rangeTo != that.rangeTo)
      return false;
    if (tag != that.tag)
      return false;
    if (!styleAttributes.equals(that.styleAttributes))
      return false;
    if (!classAttributes.equals(that.classAttributes))
      return false;

    return true;
  }

  @Override
  public String toString() {
    String queue =
      (styleAttributes.isEmpty() ? "" : "\n" + styleAttributes) + (classAttributes.isEmpty() ?
        "" :
        "\n" + classAttributes) + (otherAttributes.isEmpty() ? "" : "\n" + otherAttributes);
    return tagName().toString() + "; " + from() + "; " + to() + queue;
  }
}
