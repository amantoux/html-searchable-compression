package com.eagles.util.html;

import com.eagles.util.datastructures.Stack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO : Support '<' & '>' in plain text
 * Created by Alan Mantoux.
 */
public class HTMLSearchableCompression {

  private Stack<TagInstance> tags;
  private Stack<TagInstance> selfClosings;
  private String             plainText;

  public HTMLSearchableCompression() {
    super();
    this.tags = new Stack<>();
    this.selfClosings = new Stack<>();
  }

  public Stack<TagInstance> getSelfClosings() {
    Stack<TagInstance> tempTags = new Stack<>();
    Stack<TagInstance> cloneTags = new Stack<>();
    for (TagInstance t : selfClosings) {
      tempTags.push(t);
    }
    for (TagInstance t : tempTags) {
      cloneTags.push(t);
    }

    return cloneTags;
  }

  public static void main(String[] args) {
    HTMLSearchableCompression parser = new HTMLSearchableCompression();
    long start, end;
    String seed =
      "This is... <br><strong>REALLY <em>REALLY</em></strong><p style=\"{font-color:red;font-size:10em;}\"><em>good</em></p>";
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 1140; i++) {
      sb.append(seed);
    }
    System.out.println(seed.length());
    String toEncode = sb.toString();
    start = System.currentTimeMillis();
    parser.encode(toEncode);
    end = System.currentTimeMillis();
    int sizeInit = toEncode.length();
    int sizeEnd =
      parser.plainText.length() + parser.selfClosings.size() * 3 + ("font-color:red;font-size:10em;".length() - 4)*1000 + parser.tags.size() * 3;
    double ratio = sizeEnd * 100. / sizeInit;
    System.out.printf("Compression ratio : %2.2f%%%n", ratio);
    System.out.println("---- encoding ----");
    System.out.println("Encoded in " + (end - start) + "ms");
    /*System.out.println(parser.plainText);
    System.out.println(parser.tags);
    System.out.println(parser.selfClosings);*/
    System.out.println("");
    start = System.currentTimeMillis();
    String out = parser.decode(parser.plainText, parser.tags, parser.selfClosings);
    end = System.currentTimeMillis();
    System.out.println("---- decoding ----");
    System.out.println("Decoded in " + (end - start) + "ms");
    System.out.println("");
    System.out.println("---- diff ----");
    System.out.println(toEncode.equals(out) ? "OK" : "NOK");
    /*System.out.println(toEncode);
    System.out.println(out);*/
    if (!toEncode.equals(out)) {
      System.out.println(toEncode);
      System.out.println(out);
    }
  }

  public String getPlainText() {
    return plainText;
  }

  public Stack<TagInstance> getTags() {
    Stack<TagInstance> tempTags = new Stack<>();
    Stack<TagInstance> cloneTags = new Stack<>();
    for (TagInstance t : tags) {
      tempTags.push(t);
    }
    for (TagInstance t : tempTags) {
      cloneTags.push(t);
    }

    return cloneTags;
  }

  public String decode(String plain, Stack<TagInstance> inTags, Stack<TagInstance> sClosings) {
    this.tags = inTags;
    this.selfClosings = sClosings;
    this.plainText = plain;

    StringBuilder html = new StringBuilder();
    Stack<TagInstance> ts = new Stack<>();
    int index = plain.length();
    TagInstance t;

    /* Insert self closing tags in plain text */
    processSelfClosingTags(sClosings, html, index);

    /* Rebase input with included self closing tags */
    index = plainText.length();
    html = new StringBuilder();

    /* Process tags from the stack */
    while (tags.peek() != null) {
      t = ts.peek();

      /* if next tag is closing before the opening of the last processed one */
      if (t != null && tags.peek().to() <= t.from()) {
        index = processOpeningTags(html, ts, index);
      } else {
        index = processClosingTags(html, ts, index);
      }
    }
    while (ts.peek() != null) {
      index = processOpeningTags(html, ts, index);
    }
    return html.insert(0, plainText.substring(0, index)).toString();
  }

  private void processSelfClosingTags(Stack<TagInstance> sClosings, StringBuilder html, int index) {
    TagInstance t;
    int idx = index;
    while (sClosings.peek() != null) {
      t = sClosings.pop();
      html.insert(0, plainText.substring(t.to(), idx));
      html.insert(0, t.tagName().openingString());
      idx = t.from();
    }
    html.insert(0, plainText.substring(0, idx));
    plainText = html.toString();
  }

  private int processClosingTags(StringBuilder html, Stack<TagInstance> ts, int index) {
    TagInstance t = tags.pop();
    String s = plainText.substring(t.to(), index);
    html.insert(0, s);
    html.insert(0, t.closingString());
    ts.push(t);
    return t.to();
  }

  private int processOpeningTags(StringBuilder html, Stack<TagInstance> ts, int index) {
    TagInstance t = ts.pop();
    String s = plainText.substring(t.from(), index);
    html.insert(0, s);
    html.insert(0, t.openingString());
    return t.from();
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
    Stack<TagInstance> tempStack = new Stack<>(); // Structure to store opened tag not yet closed
    int offset = 0; // offset for regular tags
    int closingOffset = 0; // offset for closing tags

    /* decoding algorithm */
    while (m.find()) {

      String sTag = in.substring(m.start(), m.end());
      TagInstance tInstance = null;

      if (sTag.matches(Tag.getRegexOpening())) {
        tInstance = getTagOpening(m, tempStack, offset, closingOffset, sTag);
      }

      if (sTag.matches(Tag.getRegexClosing())) {
        tInstance = getTagClosing(m, tempStack, offset, sTag);
      }

      sbPlainText.append(in.substring(nextToParseIndex, m.start()));
      nextToParseIndex = m.start() + sTag.length();

      if (tInstance == null || !tInstance.tagName().isSelfClosing)
        offset += sTag.length();
      else
        closingOffset += sTag.length();

    }
    plainText = sbPlainText.append(in.substring(nextToParseIndex)).toString();
  }

  private TagInstance getTagClosing(Matcher m, Stack<TagInstance> tempStack, int offset, String sTag) {
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

  private TagInstance getTagOpening(Matcher m,
                                    Stack<TagInstance> tempStack,
                                    int offset,
                                    int closingOffset,
                                    String sTag) {
    TagInstance tInstance;
    Tag tName = Tag.isTag(sTag);
    if (tName != null && tName.isSelfClosing) {
      tInstance = new TagInstance(sTag, m.start() - closingOffset - offset);
      styleAttribute(sTag, tInstance);
      selfClosings.push(tInstance);
    } else {
      tInstance = new TagInstance(sTag, m.start() - offset);
      styleAttribute(sTag, tInstance);
      tempStack.push(tInstance);
    }
    return tInstance;
  }

  private void styleAttribute(String sTag, TagInstance t) {
    Pattern p = Pattern.compile(StyleAttribute.ATTR_REGEX);
    Matcher m = p.matcher(sTag);
    /* If there is a style attribute in the tag */
    if (m.find()) {
      /* Instantiate StyleAttribute class with style definition */
      String sAttr = sTag.substring(m.start(), m.end());
      String sMap = sAttr.split("\\{")[1].split("\\}")[0];
      for (String s : sMap.split("\\;")) {
        String[] sKeyValue = s.split("\\:");
        t.addStyleAttribute(new StyleAttribute(sKeyValue[0].trim(), sKeyValue[1].trim()));
      }
    }
  }
}
