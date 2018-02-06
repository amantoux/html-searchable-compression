package com.plato.util.html;

import com.plato.util.datastructures.InsertStringBuilder;

import java.util.Deque;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.plato.util.html.TagInstance.TAG_DELIMIT;

/**
 * Created by Alan Mantoux.
 */
public class HTMLSearchableCompression {

  static final         String ESCAPE       = "#";
  private static final String TAGS_DELIMIT = ESCAPE + "tags" + ESCAPE;
  private Deque<TagInstance> tags;
  private Deque<TagInstance> selfClosings;
  private String             plainText;

  public HTMLSearchableCompression() {
    super();
    this.tags = new LinkedList<>();
    this.selfClosings = new LinkedList<>();
  }

  public Deque<TagInstance> getTags() {
    Deque<TagInstance> tempTags = new LinkedList<>();
    Deque<TagInstance> cloneTags = new LinkedList<>();
    for (TagInstance t : tags) {
      tempTags.push(t);
    }
    for (TagInstance t : tempTags) {
      cloneTags.push(t);
    }

    return cloneTags;
  }

  public Deque<TagInstance> getSelfClosings() {
    Deque<TagInstance> tempTags = new LinkedList<>();
    Deque<TagInstance> cloneTags = new LinkedList<>();
    for (TagInstance t : selfClosings) {
      tempTags.push(t);
    }
    for (TagInstance t : tempTags) {
      cloneTags.push(t);
    }

    return cloneTags;
  }

  public String getPlainText() {
    return plainText;
  }

  static String notBewteenQuotesRegex(String s) {
    return s + "(?:(?<=[\"]" + s + ")|(?=[\"]))";
  }

  public static String decode(String plain,
                              Deque<TagInstance> inTags,
                              Deque<TagInstance> sClosings) {
    HTMLSearchableCompression c = new HTMLSearchableCompression();
    c.tags = inTags;
    c.selfClosings = sClosings;
    c.plainText = plain;

    InsertStringBuilder html = new InsertStringBuilder();
    Deque<TagInstance> ts = new LinkedList<>();
    int index = plain.length();
    TagInstance t;

    /* Insert self closing tags in plain text */
    c.processSelfClosingTags(sClosings, html, index);

    /* Rebase input with included self closing tags */
    index = c.plainText.length();
    html = new InsertStringBuilder();

    /* Process tags from the stack */
    while (c.tags.peek() != null) {
      t = ts.peek();

      /* if next tag is closing before the opening of the last processed one */
      if (t != null && c.tags.peek().to() <= t.from()) {
        index = c.processOpeningTags(html, ts, index);
      } else {
        index = c.processClosingTags(html, ts, index);
      }
    }
    while (ts.peek() != null) {
      index = c.processOpeningTags(html, ts, index);
    }
    return html.insertFirst(c.plainText.substring(0, index)).toString();
  }

  public static HTMLSearchableCompression deserializeString(String in) {
    HTMLSearchableCompression c = new HTMLSearchableCompression();
    String[] tmp = in.split(TAGS_DELIMIT);

    // if no tag
    if (tmp.length < 2) {
      return c;
    }

    for (String sTag : tmp[1].split(TAG_DELIMIT)) {
      if (!"".equals(sTag.trim()))
        c.tags.add(TagInstance.deserializeString(sTag, false));
    }

    // if no self closings
    if (tmp.length < 3)
      return c;

    for (String sTag : tmp[2].split(TAG_DELIMIT)) {
      if (!"".equals(sTag.trim()))
        c.selfClosings.add(TagInstance.deserializeString(sTag, true));
    }

    return c;
  }

  public static String decode(String plain, String tags) {
    HTMLSearchableCompression c = deserializeString(tags);
    return decode(plain, c.getTags(), c.getSelfClosings());
  }

  /**
   * Non-selfclosing tags are stored in a stack structure "tags"
   * Self closing tags are stored in a list structure "selfClosings"
   * Style attributes are stored within the tag instance
   */
  public void encode(String in) {
    Pattern pattern = Pattern.compile(Tag.getRegex());
    Matcher m = pattern.matcher(in);

    /* Helper variables */
    int nextToParseIndex = 0;
    StringBuilder sbPlainText = new StringBuilder();

    // Structure to store opened tag not yet closed
    Deque<TagInstance> tempStack = new LinkedList<>();
    // offset for regular tags
    int offset = 0;
    // offset for closing tags
    int closingOffset = 0;

    /* decoding algorithm */
    while (m.find()) {

      String sTag = in.substring(m.start(), m.end());
      TagInstance tInstance;

      if (sTag.charAt(1) != '/') {
        tInstance = getTagOpening(m, tempStack, offset, closingOffset, sTag);
      } else {
        tInstance = getTagClosing(m, tempStack, offset, sTag);
      }

      sbPlainText.append(in.substring(nextToParseIndex, m.start()));
      nextToParseIndex = m.start() + sTag.length();

      if (!tInstance.tagName().isSelfClosing)
        offset += sTag.length();
      else
        closingOffset += sTag.length();

    }
    plainText = sbPlainText.append(in.substring(nextToParseIndex)).toString();
  }

  /**
   * Assumption : their are always regular tags, selfClosing tags are optional
   *
   * @return the String format of the compression
   */
  public String serializeTagsString() {
    StringBuilder s = new StringBuilder();

    // Serialize regular tags
    s.append(TAGS_DELIMIT);
    for (TagInstance t : tags) {
      s.append(TAG_DELIMIT).append(t.serializeString());
    }

    s.append(TAGS_DELIMIT);
    for (TagInstance t : selfClosings) {
      s.append(TAG_DELIMIT).append(t.serializeString());
    }

    return s.toString();
  }

  private TagInstance getTagOpening(Matcher m,
                                    Deque<TagInstance> tempStack,
                                    int offset,
                                    int closingOffset,
                                    String sTag) {
    TagInstance tInstance;
    Tag tName = Tag.isTag(sTag);
    if (tName != null && tName.isSelfClosing) {
      tInstance = new TagInstance(sTag, m.start() - closingOffset - offset);
      tInstance.findAttributes(sTag);
      selfClosings.push(tInstance);
    } else {
      tInstance = new TagInstance(sTag, m.start() - offset);
      tInstance.findAttributes(sTag);
      tempStack.push(tInstance);
    }
    return tInstance;
  }

  private TagInstance getTagClosing(Matcher m,
                                    Deque<TagInstance> tempStack,
                                    int offset,
                                    String sTag) {
    TagInstance tInstance;
    tInstance = tempStack.pop();
    String sTagName = Tag.prepareString(sTag);

    if (tInstance == null || !tInstance.tagName().toString().equals(sTagName))
      throw new IllegalArgumentException(
        "Parser error - closing tag doesn't match current opening tag\n" + sTag);

    tInstance.setRangeTo(m.start() - offset);
    tags.push(tInstance);
    return tInstance;
  }

  private void processSelfClosingTags(Deque<TagInstance> sClosings,
                                      InsertStringBuilder html,
                                      int index) {
    TagInstance t;
    int idx = index;
    while (sClosings.peek() != null) {
      t = sClosings.pop();
      // add concat is insert as it is faster than two inserts...
      html.insertFirst(t.openingString() + plainText.substring(t.to(), idx));
      idx = t.from();
    }
    html.insertFirst(plainText.substring(0, idx));
    plainText = html.toString();
  }

  private int processOpeningTags(InsertStringBuilder html, Deque<TagInstance> ts, int index) {
    TagInstance t = ts.pop();
    String s = plainText.substring(t.from(), index);
    html.insertFirst(t.openingString() + s);
    return t.from();
  }

  private int processClosingTags(InsertStringBuilder html, Deque<TagInstance> ts, int index) {
    TagInstance t = tags.pop();
    String s = plainText.substring(t.to(), index);
    html.insertFirst(t.closingString() + s);
    ts.push(t);
    return t.to();
  }

}
