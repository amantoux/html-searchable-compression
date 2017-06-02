package com.eagles.util.html;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Alan Mantoux.
 */
public class Tag {

  private static final String OUT_OF_RANGE =
    "rangeFrom and rangeTo must both be greater or equal than 0";
  private static final String UNRECOGNIZED = " is not a recognized tag";
  private TagName              tagName;
  private List<StyleAttribute> styleAttributes;
  private int                  rangeFrom;
  private int                  rangeTo;

  public Tag(String sTagName, int rangeFrom, int rangeTo) {

    if (sTagName == null || sTagName.trim().isEmpty())
      throw new NullPointerException("Tag string cannot be null or empty");

    if (rangeFrom < 0 || rangeTo < 0)
      throw new IllegalArgumentException(OUT_OF_RANGE);

    TagName locTagName = TagName.isTag(sTagName);
    if (locTagName == null)
      throw new IllegalArgumentException(sTagName + UNRECOGNIZED);

    this.tagName = locTagName;
    this.rangeFrom = rangeFrom;
    if (tagName.isSelfClosing) {
      this.rangeTo = this.rangeFrom;
      this.styleAttributes = null;
      return;
    }
    this.rangeTo = rangeTo;
    this.styleAttributes = new LinkedList<>();
  }

  public Tag(String sTagName, int rangeFrom) {

    if (rangeFrom < 0)
      throw new IllegalArgumentException(OUT_OF_RANGE);

    TagName locTagName = TagName.isTag(sTagName);
    if (locTagName == null)
      throw new IllegalArgumentException(sTagName + UNRECOGNIZED);

    this.tagName = locTagName;
    this.rangeFrom = rangeFrom;
    if (locTagName.isSelfClosing)
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

  public TagName tagName() {
    return tagName;
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

}
