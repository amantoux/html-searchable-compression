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
  }

  public void setRangeTo(int to) {
    rangeTo = to;
  }

  public String openingString() {
    StringBuilder openingString = new StringBuilder("<" + tagName().toString());
    if (!styleAttributes.isEmpty()) {
      openingString.append(" style=\"{");
      for (StyleAttribute s : styleAttributes) {
        openingString.append(s.toString()).append(";");
      }
      openingString.append("}\"");
    }
    return openingString.append(">").toString();
  }

  public Tag tagName() {
    return tag;
  }

  public String closingString() {
    return tagName().closingString();
  }

  @Override
  public String toString() {
    return tagName().toString() + "; " + from() + "; " + to() + "\n" + styleAttributes;
  }


  public int from() {
    return rangeFrom;
  }

  public int to() {
    return rangeTo;
  }

  public void addStyleAttribute(StyleAttribute s) {
    styleAttributes.add(s);
  }

  public List<StyleAttribute> getStyleAttribute() {
    return styleAttributes;
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

    return true;
  }

  @Override
  public int hashCode() {
    int result = tag.hashCode();
    result = 31 * result + styleAttributes.hashCode();
    result = 31 * result + rangeFrom;
    result = 31 * result + rangeTo;
    return result;
  }
}
