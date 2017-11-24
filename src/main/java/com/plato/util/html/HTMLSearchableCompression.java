package com.plato.util.html;

import com.plato.util.datastructures.InsertStringBuilder;

import java.text.DecimalFormat;
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

  public static void main(String[] args) {
    HTMLSearchableCompression parser = new HTMLSearchableCompression();
    long start, end;
    String seed =
      "<p>Alors?! on se la prend cette bière???</p><p>Je suis <strong>hyper</strong> saoulé...</p><p><span class=\"ql-size-large\" style=\"color: rgb(0, 138, 0);\">Vraiment?....</span></p><p><span class=\"ql-size-large\" style=\"color: rgb(230, 0, 0);\">Hein? re re quoi? ?</span></p><p><span class=\"ql-size-large\" style=\"color: rgb(102, 163, 224);\">Oui qyoi</span></p>";
    StringBuilder sb = new StringBuilder();
    int nbRepet = 100_000;
    for (int i = 0; i < nbRepet; i++) {
      sb.append(seed);
    }
    System.out.println("Init string length : " + seed.length());
    System.out.println("Number of repetitions : " + nbRepet);
    System.out.println(
      "Estimated size of file : " + seed.length() * nbRepet * 16 /* char size*/ / 1024 / 1024
        + "Mo");
    String toEncode = sb.toString();

    start = System.currentTimeMillis();
    parser.encode(toEncode);
    end = System.currentTimeMillis();

    long sizeInit = (long) (toEncode.length()) * 16;
    long sizeEnd = parser.computeSize();
    double ratio = sizeEnd * 100. / sizeInit;

    DecimalFormat myFormatter = new DecimalFormat("###,###,###");
    String sSizeInit = myFormatter.format(sizeInit);
    String sSizeEnd = myFormatter.format(sizeEnd);
    System.out.printf("Compression ratio : %2.2f%%%n", ratio);
    System.out.println("Start size : " + sSizeInit + " bits");
    System.out.println("End size : " + sSizeEnd + " bits\n");

    System.out.println("---- encoding ----");
    System.out.println("Encoded in " + (end - start) + "ms");
    System.out.println("");

    start = System.currentTimeMillis();
    HTMLSearchableCompression.decode(parser.plainText, parser.tags, parser.selfClosings);
    end = System.currentTimeMillis();

    System.out.println("---- decoding ----");
    System.out.println("Decoded in " + (end - start) + "ms");
    System.out.println("");

    parser.encode(toEncode);
    start = System.currentTimeMillis();
    String serialString = parser.serializeTagsString();
    end = System.currentTimeMillis();

    System.out.println("---- Serializing ---");
    System.out.println("Serialize in " + (end - start) + "ms");
    System.out.println("");

    start = System.currentTimeMillis();
    HTMLSearchableCompression.deserializeString(serialString);
    end = System.currentTimeMillis();

    System.out.println("---- Deserializing ---");
    System.out.println("Deserialize in " + (end - start) + "ms");
    System.out.println("");
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

  /*
      Non-selfclosing tags are stored in a stack structure "tags"
      Self closing tags are stored in a list structure "selfClosings"
      Style attributes are stored within the tag instance
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

  private long computeSize() {
    long size = plainText.length() * (long) 16;
    for (TagInstance t : tags) {
      size += 32 * 2 + t.tagName().toString().length() * 16;
      for (StyleAttribute s : t.getStyleAttributes()) {
        size += (s.getKey().length() + s.getValue().length()) * 16;
      }
      for (ClassAttribute c : t.getClassAttributes()) {
        size += (c.getValue().length()) * 16;
      }
    }
    for (TagInstance t : selfClosings) {
      size += 32 * 2 + t.tagName().toString().length() * 16;
    }
    return size;
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
      findAttributes(sTag, tInstance);
      selfClosings.push(tInstance);
    } else {
      tInstance = new TagInstance(sTag, m.start() - offset);
      findAttributes(sTag, tInstance);
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
      html.insertFirst(t.tagName().openingString() + plainText.substring(t.to(), idx));
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

  private void findAttributes(String sTag, TagInstance t) {
    Pattern p = Pattern.compile(
      "(" + ClassAttribute.CLASS_REGEX + ")" + "|" + "(" + StyleAttribute.STYLE_REGEX + ")" + "|"
        + "(" + Attribute.ATTR_REGEX + ")");
    Matcher m = p.matcher(sTag);
    /* If there is a attribute in the tag */
    while (m.find()) {
      /* Instantiate Attribute class with style definition */
      String sAttr = sTag.substring(m.start(), m.end());
      if (sAttr.startsWith("class")) {
        classAttribute(sAttr, t);
      } else if (sAttr.startsWith("style")) {
        styleAttribute(sAttr, t);
      } else {
        attribute(sAttr, t);
      }
    }
  }

  private void classAttribute(String sAttr, TagInstance t) {
    String sSet = sAttr.split("\"")[1].split("\"")[0];
    for (String s : sSet.split(" ")) {
      // handle case "class1  class2" (double space)
      if ("".equals(s.trim()))
        continue;
      t.addClassAttribute(new ClassAttribute(s));
    }
  }

  private void styleAttribute(String sAttr, TagInstance t) {
    String sMap = sAttr.split("\"(\\{)?")[1].split("(\\})?\"")[0];
    for (String s : sMap.split("\\;")) {
      String[] sKeyValue = s.split("\\:");
      t.addStyleAttribute(new StyleAttribute(sKeyValue[0].trim(), sKeyValue[1].trim()));
    }
  }

  private void attribute(String sAttr, TagInstance t) {
    String[] keyValue = sAttr.split("=");
    String key = keyValue[0].trim();
    String value = keyValue[1].split("\"")[1].split("\"")[0].trim();
    t.addAttribute(new Attribute(key, value));
  }
}
