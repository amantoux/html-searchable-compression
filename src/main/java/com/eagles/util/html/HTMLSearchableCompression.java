package com.eagles.util.html;

import com.eagles.util.datastructures.InsertStringBuilder;
import com.eagles.util.datastructures.Stack;

import javax.swing.text.html.HTML;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Scanner;
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

  public long computeSize() {
    long size = plainText.length();
    for (TagInstance t : tags) {
      size += 32 * 2 + t.tagName().toString().length();
      for (StyleAttribute s : t.getStyleAttribute()) {
        size += s.getKey().length() + s.getValue().length();
      }
    }
    for(TagInstance t : selfClosings) {
      size += 32 * 2 + t.tagName().toString().length();
    }
    return size;
  }

  public static void main(String[] args) {
    HTMLSearchableCompression parser = new HTMLSearchableCompression();
    Scanner s = new Scanner(System.in);
    long start, end;
    String seed =
      "This is... <br><strong>REALLY <em>REALLY</em></strong><p style=\"{font-color:red;font-size:10em;}\"><em>good</em></p>";
    StringBuilder sb = new StringBuilder();
    int nbRepet = 1_000_000;
    for (int i = 0; i < nbRepet; i++) {
      sb.append(seed);
    }
    System.out.println("Init string length : " + seed.length());
    System.out.println("Number of repetitions : " + nbRepet);
    System.out.println(
      "Estimated size of file : " + seed.length() * nbRepet * 16 /* char size*/ / 1024 / 1024 + "Mo");
    String toEncode = sb.toString();
    System.out.println("Hit enter to start");
    s.nextLine();

    start = System.currentTimeMillis();
    parser.encode(toEncode);
    end = System.currentTimeMillis();

    long sizeInit = toEncode.length() * 16;
    long sizeEnd = parser.computeSize();
    double ratio = sizeEnd * 100. / sizeInit;

    DecimalFormat myFormatter = new DecimalFormat("###,###,###");
    String sSizeInit = myFormatter.format(sizeInit);
    String sSizeEnd = myFormatter.format(sizeEnd);
    System.out.printf("Compression ratio : %2.2f%%%n", ratio);
    System.out.println("Start size : " +sSizeInit + " bits");
    System.out.println("End size : " +sSizeEnd + " bits\n");

    System.out.println("---- encoding ----");
    System.out.println("Encoded in " + (end - start) + "ms");
    System.out.println("");

    HTMLSearchableCompression decodeParser = new HTMLSearchableCompression();
    start = System.currentTimeMillis();
    String out = decodeParser.decode(parser.plainText, parser.tags, parser.selfClosings);
    end = System.currentTimeMillis();

    System.out.println("---- decoding ----");
    System.out.println("Decoded in " + (end - start) + "ms");
    System.out.println("");
    System.out.println("---- diff ----");
    System.out.println(toEncode.equals(out) ? "OK" : "NOK");
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

    InsertStringBuilder html = new InsertStringBuilder();
    Stack<TagInstance> ts = new Stack<>();
    int index = plain.length();
    TagInstance t;

    /* Insert self closing tags in plain text */
    processSelfClosingTags(sClosings, html, index);

    /* Rebase input with included self closing tags */
    index = plainText.length();
    html = new InsertStringBuilder();

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
    return html.insertFirst(plainText.substring(0, index)).toString();
  }

  private void processSelfClosingTags(Stack<TagInstance> sClosings,
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

  private int processClosingTags(InsertStringBuilder html, Stack<TagInstance> ts, int index) {
    TagInstance t = tags.pop();
    String s = plainText.substring(t.to(), index);
    html.insertFirst(t.closingString() + s);
    ts.push(t);
    return t.to();
  }

  private int processOpeningTags(InsertStringBuilder html, Stack<TagInstance> ts, int index) {
    TagInstance t = ts.pop();
    String s = plainText.substring(t.from(), index);
    html.insertFirst(t.openingString() + s);
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

  private TagInstance getTagClosing(Matcher m,
                                    Stack<TagInstance> tempStack,
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
