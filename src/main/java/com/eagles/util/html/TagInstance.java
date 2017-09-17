package com.eagles.util.html;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Alan Mantoux.
 */
public class TagInstance {

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

  public String openingString() {
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

  public Tag tagName() {
    return tag;
  }

  public String closingString() {
    return tagName().closingString();
  }

  public void addStyleAttribute(StyleAttribute s) {
    styleAttributes.add(s);
  }

  public void addClassAttribute(ClassAttribute c) {
    classAttributes.add(c);
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
    String queue = (styleAttributes.isEmpty() ? "" : "\n" + styleAttributes) + (classAttributes.isEmpty() ? "" : "\n" + classAttributes);
    return tagName().toString() + "; " + from() + "; " + to() + queue;
  }

  public int from() {
    return rangeFrom;
  }

  public int to() {
    return rangeTo;
  }

  public List<StyleAttribute> getStyleAttributes() {
    return styleAttributes;
  }

  public List<ClassAttribute> getClassAttributes() {
    return classAttributes;
  }

  public void setRangeTo(int to) {
    rangeTo = to;
  }
}
