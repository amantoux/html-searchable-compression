package com.eagles.util.html;

import com.eagles.util.datastructures.Stack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO : Support style attributes
 * TODO : Support '<' & '>' in plain text
 * Created by Alan Mantoux.
 */
public class HTMLSearchCompressor {

  private Stack<Tag> tags;
  private Stack<Tag> selfClosings;
  private String     plainText;

  public HTMLSearchCompressor() {
    super();
    this.tags = new Stack<>();
    this.selfClosings = new Stack<>();
  }

  public static void main(String[] args) {
    HTMLSearchCompressor parser = new HTMLSearchCompressor();
    long start, end;
    String toEncode = "This is... <br><strong>REALLY <em>REALLY</em></strong><p style=\"{font-color:red;font-size:10em;}\">good</p>";
    start = System.currentTimeMillis();
    parser.encode(toEncode);
    end = System.currentTimeMillis();
    long sizeInit = toEncode.length();
    long sizeEnd = parser.plainText.length() + parser.selfClosings.size() + parser.tags.size();
    double ratio = sizeEnd * 100. / sizeInit;
    System.out.printf("Compression ratio : %2.2f%%%n", ratio);
    System.out.println("---- encoding ----");
    System.out.println("Encoded in " + (end - start) + "ms");
    System.out.println(parser.plainText);
    System.out.println(parser.tags);
    System.out.println("");
    start = System.currentTimeMillis();
    String out = parser.decode(parser.plainText, parser.tags, parser.selfClosings);
    end = System.currentTimeMillis();
    System.out.println("---- decoding ----");
    System.out.println("Decoded in " + (end - start) + "ms");
    System.out.println("");
    System.out.println("---- diff ----");
    System.out.println(toEncode.equals(out) ? "OK" : "NOK");
    System.out.println(toEncode);
    System.out.println(out);
    if (!toEncode.equals(out)) {
      System.out.println(toEncode);
      System.out.println(out);
    }
  }

  public String decode(String plain, Stack<Tag> inTags, Stack<Tag> sClosings) {
    this.tags = inTags;
    this.selfClosings = sClosings;
    this.plainText = plain;

    StringBuilder html = new StringBuilder();
    Stack<Tag> ts = new Stack<>();
    int index = plain.length();
    Tag t;

    /* Insert self closing tags in plain text */
    processSelfClosingTags(sClosings, html, index);

    /* Rebase input with included self closing tags */
    index = plainText.length();
    html = new StringBuilder();

    /* Process tags from the stack */
    while (tags.peek() != null) {
      t = ts.peek();
      boolean isNextTagClosingBeforeLastProcessed = t != null && tags.peek().to() <= t.from();
      /*  */
      if (isNextTagClosingBeforeLastProcessed) {
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

  private void processSelfClosingTags(Stack<Tag> sClosings, StringBuilder html, int index) {
    Tag t;
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

  private int processClosingTags(StringBuilder html, Stack<Tag> ts, int index) {
    Tag t = tags.pop();
    String s = plainText.substring(t.to(), index);
    html.insert(0, s);
    html.insert(0, t.closingString());
    ts.push(t);
    return t.to();
  }

  private int processOpeningTags(StringBuilder html, Stack<Tag> ts, int index) {
    Tag t = ts.pop();
    String s = plainText.substring(t.from(), index);
    html.insert(0, s);
    html.insert(0, t.openingString());
    return t.from();
  }

  public void encode(String in) {
    Pattern pattern = Pattern.compile(TagName.getRegex());
    Matcher m = pattern.matcher(in);

    /* Helper variables */
    int nextToParseIndex = 0;
    StringBuilder sbPlainText = new StringBuilder();
    Stack<Tag> tempStack = new Stack<>();
    int offset = 0;
    int closingOffset = 0;

    /* decoding algorithm */
    while (m.find()) {

      String sTag = in.substring(m.start(), m.end());
      Tag nextTag = null;

      if (sTag.matches(TagName.getRegexOpening())) {
        nextTag = getTagOpening(m, tempStack, offset, closingOffset, sTag);
      }

      if (sTag.matches(TagName.getRegexClosing())) {
        nextTag = getTagClosing(m, tempStack, offset, sTag);
      }

      sbPlainText.append(in.substring(nextToParseIndex, m.start()));
      nextToParseIndex = m.start() + sTag.length();

      if (nextTag == null || !nextTag.tagName().isSelfClosing)
        offset += sTag.length();
      else
        closingOffset += sTag.length();

    }
    plainText = sbPlainText.append(in.substring(nextToParseIndex)).toString();
  }

  private Tag getTagClosing(Matcher m, Stack<Tag> tempStack, int offset, String sTag) {
    Tag nextTag;
    nextTag = tempStack.pop();
    String sTagName = TagName.prepareString(sTag);

    if (nextTag == null || !nextTag.tagName().toString().equals(sTagName))
      throw new IllegalArgumentException(
        "Parser error - closing tag doesn't match current opening tag\n" + sTag);

    nextTag.setRangeTo(m.start() - offset);
    tags.push(nextTag);
    return nextTag;
  }

  private Tag getTagOpening(Matcher m,
                            Stack<Tag> tempStack,
                            int offset,
                            int closingOffset,
                            String sTag) {
    Tag nextTag;
    TagName tName = TagName.isTag(sTag);
    if (tName != null && tName.isSelfClosing) {
      nextTag = new Tag(sTag, m.start() - closingOffset - offset);
      styleAttribute(sTag, nextTag);
      selfClosings.push(nextTag);
    } else {
      nextTag = new Tag(sTag, m.start() - offset);
      styleAttribute(sTag, nextTag);
      tempStack.push(nextTag);
    }
    return nextTag;
  }

  private void styleAttribute(String sTag, Tag t) {
    Pattern p = Pattern.compile(StyleAttribute.ATTR_REGEX);
    Matcher m = p.matcher(sTag);
    if (m.find()) {
      String sAttr = sTag.substring(m.start(), m.end());
      String sMap = sAttr.split("\\{")[1].split("\\}")[0];
      for (String s : sMap.split("\\;")) {
        String[] sKeyValue = s.split("\\:");
        t.addStyleAttribute(new StyleAttribute(sKeyValue[0].trim(), sKeyValue[1].trim()));
      }
    }
  }
}
