package com.eagles.util.html;

import com.plato.util.html.HTMLSearchableCompression;
import com.plato.util.html.StyleAttribute;
import com.plato.util.html.TagInstance;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Deque;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;

/**
 * Created by Alan Mantoux.
 */
public class HTMLSearchableCompressionTest {

  private static String             toEncode     =
    "This is... <br><strong>REALLY <em>REALLY</em></strong><p style=\"font-color:red;font-size:10em;\"><em>good</em></p>123";
  private static String             plainText    = "This is... REALLY REALLYgood123";
  private static Deque<TagInstance> tags         = new LinkedList<>();
  private static Deque<TagInstance> selfClosings = new LinkedList<>();
  private static HTMLSearchableCompression parser;
  private static String stringSerial =
    "#tags##tagp;28;32#stylefont-color#:red;font-size#:10em#tagem;28;32#tagstrong;15;28#tagem;22;28#tags##tagbr;11";

  @BeforeClass
  public static void initAll() {

  }

  @Before
  public void init() {
    tags = new LinkedList<>();
    selfClosings = new LinkedList<>();
    parser = new HTMLSearchableCompression();
    tags.push(new TagInstance("<em>", 22, 28));
    tags.push(new TagInstance("<strong>", 15, 28));
    tags.push(new TagInstance("<em>", 28, 32));
    TagInstance p = new TagInstance("<p>", 28, 32);
    p.addStyleAttribute(new StyleAttribute("font-color", "red"));
    p.addStyleAttribute(new StyleAttribute("font-size", "10em"));
    tags.push(p);
    selfClosings.push(new TagInstance("<br>", 11));
  }

  @Test
  public void decode() throws Exception {
    assertEquals("Decoding : ", toEncode,
      HTMLSearchableCompression.decode(plainText, tags, selfClosings));
  }

  @Test
  public void encode() throws Exception {
    parser.encode(toEncode);
    assertEquals("Encoding - plain text : ", plainText, parser.getPlainText());
    assertEquals("Encoding - tags : ", tags, parser.getTags());
    assertEquals("Encoding - selfclosing tags : ", selfClosings, parser.getSelfClosings());
  }

  @Test
  public void serializeString() throws Exception {
    parser.encode(toEncode);
    assertEquals("Serialize : ", stringSerial, parser.serializeTagsString());
  }

  @Test
  public void deserializeString() throws Exception {
    parser.encode(toEncode);
    String exp = parser.serializeTagsString();
    String obs = HTMLSearchableCompression.deserializeString(exp).serializeTagsString();
    assertEquals("Deserialize : ", exp, obs);
  }

}
