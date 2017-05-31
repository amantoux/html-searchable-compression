package com.eagles.util.html;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Alan Mantoux.
 */
public class Tag {

  public static final String OUT_OF_RANGE =
    "rangeFrom and rangeTo must both be greater or equal than 0";
  public static final String UNRECOGNIZED = " is not a recognized tag";
  private TagName tagName;
  private Set<StyleAttribute> styleAttributes;
  private int                 rangeFrom;
  private int                 rangeTo;
  private boolean             inNext; // For self-closing "<br><p>...</p>

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
    this.styleAttributes = new HashSet<>();
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
    this.inNext = true;
  }

  public boolean inNext() {
    return this.inNext;
  }

  public Tag(String sTagName, int rangeFrom, boolean inNext) {
    if (rangeFrom < 0)
      throw new IllegalArgumentException(OUT_OF_RANGE);

    TagName locTagName = TagName.isTag(sTagName);
    if (locTagName == null)
      throw new IllegalArgumentException(sTagName + UNRECOGNIZED);

    if (locTagName.isSelfClosing)
      throw new IllegalArgumentException(tagName + " is not a closing tag, please use another constructor");

    this.tagName = locTagName;
    this.rangeFrom = rangeFrom;
    this.rangeTo = rangeFrom;
    this.inNext = inNext;
  }

  public void setRangeTo(int to) {
    rangeTo = to;
  }

  @Override
  public String toString() {
    return tagName().toString() + "; " + from() + "; " + to();
  }

  public TagName tagName() {
    return tagName;
  }

  public int from() {
    return rangeFrom;
  }

  public int to() {
    return rangeTo;
  }

}
