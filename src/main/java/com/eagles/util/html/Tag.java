package com.eagles.util.html;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Alan Mantoux.
 */
public class Tag {

  private TagName             tagName;
  private Set<StyleAttribute> styleAttributes;
  private int                 rangeFrom;
  private int                 rangeTo;

  public Tag(String sTagName, int rangeFrom, int rangeTo) {

    if (sTagName == null || sTagName.trim().isEmpty())
      throw new NullPointerException("Tag string cannot be null or empty");

    if (rangeFrom < 0 || rangeTo < 0)
      throw new IllegalArgumentException("rangeFrom and rangeTo must both be greater or equal than 0");

    TagName locTagName = TagName.isTag(sTagName);
    if (locTagName == null)
      throw new IllegalArgumentException(sTagName + " is not a recognized tag");

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
      throw new IllegalArgumentException("rangeFrom and rangeTo must both be greater or equal than 0");

    TagName locTagName = TagName.isTag(sTagName);
    if (locTagName == null)
      throw new IllegalArgumentException(sTagName + " is not a recognized tag");

    /*
    if (locTagName.isSelfClosing)
      throw new IllegalArgumentException(tagName + " is not a closing tag, please use another constructor");
    */

    this.tagName = locTagName;
    this.rangeFrom = rangeFrom;
    if (locTagName.isSelfClosing)
      this.rangeTo = rangeFrom;
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

  public void setRangeTo(int to) {
    rangeTo = to;
  }

  @Override
  public String toString() {
    return tagName().toString() + "; " + from() + "; " + to();
  }

}
