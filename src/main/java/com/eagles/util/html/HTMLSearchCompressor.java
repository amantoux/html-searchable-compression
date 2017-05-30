package com.eagles.util.html;

import com.eagles.util.datastructures.Stack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Alan Mantoux.
 */
public class HTMLSearchCompressor {

  private String     input;
  private Stack<Tag> tagStack;
  private String     plainText;

  public HTMLSearchCompressor() {
    super();
    this.input = null;
    this.tagStack = new Stack<>();
  }

  public static void main(String[] args) {
    HTMLSearchCompressor parser = new HTMLSearchCompressor();
    String toEncode = "This is a test, <p><br><strong>s</strong>ay</p><p>\"Hello world\"</p>";
    System.out.println(toEncode);
    System.out.println("");
    parser.encode(toEncode);
    System.out.println("---- encoding ----");
    System.out.println(parser.plainText);
    System.out.println(parser.tagStack);
    System.out.println("");
    String out = parser.decode(parser.plainText, parser.tagStack);
    System.out.println("---- decoding ----");
    System.out.println(out);
    System.out.println("");
    System.out.println("---- diff ----");
    System.out.println(toEncode.equals(out) ? "OK" : "NOK");
    if (!toEncode.equals(out)) {
      System.out.println(toEncode);
      System.out.println(out);
    }
  }

  public String decode(String plain, Stack<Tag> tags) {

    StringBuilder html = new StringBuilder();
    Stack<Tag> ts = new Stack<>();
    int index = plain.length();

    while (tags.peek() != null) {
      Tag t = ts.peek();
      if (t != null && tags.peek().to() <= t.from()) {
        index = processOpeningTags(html, ts, index);
      } else {
        index = processClosingTags(html, ts, index);
      }
    }
    while (ts.peek() != null) {
      index = processOpeningTags(html, ts, index);
    }
    return html.insert(0, plain.substring(0, index)).toString();
  }

  private int processClosingTags(StringBuilder html, Stack<Tag> ts, int index) {
    Tag t = tagStack.pop();
    String s = plainText.substring(t.to(), index);
    html.insert(0, s);
    html.insert(0, t.tagName().closingString());
    if (!t.tagName().isSelfClosing)
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
    int currentCumulatedOffset = 0;

    /* decoding algorithm */
    while (m.find()) {
      String sTag = in.substring(m.start(), m.end());

      if (sTag.matches(TagName.getRegexOpening())) {
        Tag nextTag = new Tag(sTag, m.start() - currentCumulatedOffset);
        if (nextTag.tagName().isSelfClosing) {
          tagStack.push(nextTag);
        } else {
          tempStack.push(nextTag);
        }
      }

      if (sTag.matches(TagName.getRegexClosing())) {
        Tag nextTag = tempStack.pop();
        String sTagName = TagName.prepareString(sTag);

        if (nextTag == null || !nextTag.tagName().toString().equals(sTagName))
          throw new IllegalArgumentException(
            "Parser error - closing tag doesn't match current opening tag\n" + sTag);

        nextTag.setRangeTo(m.start() - currentCumulatedOffset);
        tagStack.push(nextTag);
      }
      sbPlainText.append(in.substring(nextToParseIndex, m.start()));
      nextToParseIndex = m.start() + sTag.length();
      currentCumulatedOffset += sTag.length();
    }
    plainText = sbPlainText.toString();
  }
}
