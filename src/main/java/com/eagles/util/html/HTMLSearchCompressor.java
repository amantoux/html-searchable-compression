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

  public String decode(String plain, Stack<Tag> tags) {
    plainText = plain;
    tagStack = tags;
    StringBuilder html = new StringBuilder();
    Stack<Tag> tempStack = new Stack<>();
    int processedIndex = plainText.length();

    while (tagStack.peek() != null) {
      Tag nextTag = tagStack.pop();
      String sub = plainText.substring(nextTag.getRangeTo(), processedIndex);
      processedIndex = nextTag.getRangeTo();
      html.insert(0, sub);
      html.insert(0, nextTag.getTagName().closingString());
      if (!nextTag.getTagName().isSelfClosing)
        tempStack.push(nextTag);
    }
    System.out.println("After first stack: " + html.toString());
    while (tempStack.peek() != null) {
      Tag nextTag = tempStack.pop();
      String sub = plainText.substring(nextTag.getRangeFrom(), processedIndex);
      html.insert(0, sub);
      html.insert(0, nextTag.getTagName().openingString());
    }
    return html.toString();
  }

  public static void main(String[] args) {
    HTMLSearchCompressor parser = new HTMLSearchCompressor();
    String toEncode = "This is a test, say<br><p>\"Hello <strong>wor<em>ld</em></strong>\"</p>";
    System.out.println(toEncode);
    parser.encode(toEncode);
    System.out.println("---- encoding ----");
    System.out.println(parser.plainText);
    System.out.println(parser.tagStack);
    String out = parser.decode(parser.plainText, parser.tagStack);
    System.out.println("---- decoding ----");
    System.out.println(out);



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
        if (nextTag.getTagName().isSelfClosing) {
          tagStack.push(nextTag);
        } else {
          tempStack.push(nextTag);
        }
      }

      if (sTag.matches(TagName.getRegexClosing())) {
        Tag nextTag = tempStack.pop();
        String sTagName = TagName.prepareString(sTag);

        if (nextTag == null || !nextTag.getTagName().toString().equals(sTagName))
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
