package com.eagles.util.html;

import com.eagles.util.datastructures.Stack;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Alan Mantoux.
 */
public class HTMLSearchableCompressionTest {

  public static String             toEncode     =
    "This is... <br><strong>REALLY <em>REALLY</em></strong><p style=\"{font-color:red;font-size:10em;}\"><em>good</em></p>123";
  public static String             plainText    = "This is... REALLY REALLYgood123";
  public static Stack<TagInstance> tags         = new Stack<>();
  public static Stack<TagInstance> selfClosings = new Stack<>();
  public static HTMLSearchableCompression parser;

  @BeforeClass
  public static void initAll() {

  }

  @Before
  public void init() {
    tags = new Stack<>();
    selfClosings = new Stack<>();
    parser = new HTMLSearchableCompression();
    tags.push(new TagInstance("<em>", 22, 28));
    tags.push(new TagInstance("<strong>", 15, 28));
    tags.push(new TagInstance("<em>", 28,32));
    TagInstance p = new TagInstance("<p>", 28,32);
    p.addStyleAttribute(new StyleAttribute("font-color", "red"));
    p.addStyleAttribute(new StyleAttribute("font-size", "10em"));
    tags.push(p);
    selfClosings.push(new TagInstance("<br>", 11));
  }

  @Test
  public void decode() throws Exception {
    assertEquals("Decoding : ", toEncode, parser.decode(plainText, tags, selfClosings));
  }

  @Test
  public void encode() throws Exception {
    parser.encode(toEncode);
    assertEquals("Encoding - plain text : ", plainText, parser.getPlainText());
    assertEquals("Encoding - tags : ", tags, parser.getTags());
    assertEquals("Encoding - selfclosing tags : ", selfClosings, parser.getSelfClosings());
  }

}
