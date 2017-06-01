package com.eagles.util.html;

import com.eagles.util.datastructures.Stack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Alan Mantoux.
 */
public class HTMLSearchCompressor {

  private String     input;
  private Stack<Tag> tags;
  private Stack<Tag> selfClosings;
  private String     plainText;

  public HTMLSearchCompressor() {
    super();
    this.input = null;
    this.tags = new Stack<>();
    this.selfClosings = new Stack<>();
  }

  public static void main(String[] args) {
    HTMLSearchCompressor parser = new HTMLSearchCompressor();
    long start, end;
    String toEncode = "This is a test<br>Say<p>\"<em>Hello</em> <strong>world</strong>\"</p>123"
      + "This is a test<br>Say<p>\"Hello world\"</p>This is a test<br>Say<p>\"<em>Hello</em> <strong>world</strong>\"</p>123";
    start = System.currentTimeMillis();
    parser.encode(toEncode);
    end = System.currentTimeMillis();
    long sizeInit = toEncode.length();
    long sizeEnd = parser.plainText.length() + parser.selfClosings.size() + parser.tags.size();
    double ratio = sizeEnd*100./sizeInit;
    System.out.printf("Compression ratio : %2.2f%%%n", ratio);
    System.out.println("---- encoding ----");
    System.out.println("Encoded in " + (end - start) + "ms");
    System.out.println(parser.plainText);
    System.out.println("");
    start = System.currentTimeMillis();
    String out = parser.decode(parser.plainText, parser.tags, parser.selfClosings);
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
    html.insert(0, t.tagName().closingString());
    ts.push(t);
    return t.to();
  }

  private int processOpeningTags(StringBuilder html, Stack<Tag> ts, int index) {
    Tag t = ts.pop();
    String s = plainText.substring(t.from(), index);
    html.insert(0, s);
    html.insert(0, t.tagName().openingString());
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

        if (TagName.isTag(sTag).isSelfClosing) {
          nextTag = new Tag(sTag, m.start() - closingOffset - offset);
          selfClosings.push(nextTag);
        } else {
          nextTag = new Tag(sTag, m.start() - offset);
          tempStack.push(nextTag);
        }
      }

      if (sTag.matches(TagName.getRegexClosing())) {
        nextTag = tempStack.pop();
        String sTagName = TagName.prepareString(sTag);

        if (nextTag == null || !nextTag.tagName().toString().equals(sTagName))
          throw new IllegalArgumentException(
            "Parser error - closing tag doesn't match current opening tag\n" + sTag);

        nextTag.setRangeTo(m.start() - offset);
        tags.push(nextTag);
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
}
