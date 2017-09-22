package com.plato.util.html;

import java.util.LinkedList;
import java.util.List;

import static com.plato.util.html.ClassAttribute.*;
import static com.plato.util.html.HTMLSearchableCompression.ESCAPE;
import static com.plato.util.html.StyleAttribute.*;

/**
 * Created by Alan Mantoux.
 */
public class TagInstance {

  static final         String TAG_DELIMIT  = ESCAPE + "tag";
  private static final String OUT_OF_RANGE =
    "rangeFrom and rangeTo must both be greater or equal than 0";
  private static final String UNRECOGNIZED = " is not a recognized tag";
  private Tag                  tag;
  private List<StyleAttribute> styleAttributes;
  private List<ClassAttribute> classAttributes;
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
  }

  private TagInstance(Tag tag, int rangeFrom, int rangeTo) {
    if (rangeFrom < 0 || rangeTo < 0)
      throw new IllegalArgumentException(OUT_OF_RANGE);
    this.tag = tag;
    this.rangeFrom = rangeFrom;
    this.rangeTo = rangeTo;
    this.styleAttributes = new LinkedList<>();
    this.classAttributes = new LinkedList<>();
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
  }

  static TagInstance deserializeString(String in, boolean selfClosing) {
    if (selfClosing) {
      String[] sTagProps = in.split(";");
      return new TagInstance(Tag.getInstance(sTagProps[0]), Integer.parseInt(sTagProps[1]));
    }
    // Isolate tag props
    // it will always start with style
    String regexStyleOrClass = "(" + STYLE_DELIMIT + ")|(" + CLASS_DELIMIT + ")";
    String[] sTemp = in.split(regexStyleOrClass);
    String[] sTagProps = sTemp[0].split(";");
    TagInstance t = new TagInstance(Tag.getInstance(sTagProps[0]), Integer.parseInt(sTagProps[1]),
      Integer.parseInt(sTagProps[2]));

    if (sTemp.length < 2 || "".equals(sTemp[1].trim()))
      return t;

    boolean hasStyle = in.contains(STYLE_DELIMIT);
    boolean hasClass = in.contains(CLASS_DELIMIT);
    int indexOfClass = hasStyle ? 2 : 1;
    if (hasStyle) {
      // parse style
      String[] sStyles = sTemp[1].split(";");
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

    return t;
  }

  public void addStyleAttribute(StyleAttribute s) {
    styleAttributes.add(s);
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

    return openingString.append(">").toString();
  }

  Tag tagName() {
    return tag;
  }

  String closingString() {
    return tagName().closingString();
  }

  String serializeString() {
    if (tag.isSelfClosing)
      return tag.toString() + ";" + from();
    StringBuilder s = new StringBuilder();
    s.append(tag.toString()).append(";").append(from()).append(";").append(to());
    addAttributesToStringBuilder(s, styleAttributes, STYLE_DELIMIT);
    addAttributesToStringBuilder(s, classAttributes, CLASS_DELIMIT);
    return s.toString();
  }

  int from() {
    return rangeFrom;
  }

  int to() {
    return rangeTo;
  }

  private void addAttributesToStringBuilder(StringBuilder s,
                                            List<? extends Attribute> attributes,
                                            String attrTag) {
    if (!attributes.isEmpty()) {
      s.append(attrTag);
      for (Attribute a : attributes) {
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
        "\n" + classAttributes);
    return tagName().toString() + "; " + from() + "; " + to() + queue;
  }

  List<StyleAttribute> getStyleAttributes() {
    return styleAttributes;
  }

  List<ClassAttribute> getClassAttributes() {
    return classAttributes;
  }

  void setRangeTo(int to) {
    rangeTo = to;
  }
}
