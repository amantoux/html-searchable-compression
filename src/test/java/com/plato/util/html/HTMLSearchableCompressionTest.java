package com.plato.util.html;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Deque;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;

/**
 * Created by Alan Mantoux.
 */
public class HTMLSearchableCompressionTest {

  private static String             toEncode     =
    "This is... <br><strong>REALLY <em>REALLY</em></strong><p style=\"font-color:red;font-size:10em\"><em>good</em></p>123";
  private static String             plainText    = "This is... REALLY REALLYgood123";
  private static Deque<TagInstance> tags         = new LinkedList<>();
  private static Deque<TagInstance> selfClosings = new LinkedList<>();
  private static HTMLSearchableCompression parser;
  private static String stringSerial          =
    "#tags##tagp;28;32#stylefont-color#:red;font-size#:10em#tagem;28;32#tagstrong;15;28#tagem;22;28#tags##tagbr;11";
  private static String toEncodeOnlySelf      = "This<br> is... REALLY REALLYgood123";
  private static String stringOnlySelfClosing = "#tags##tags##tagbr;4";

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

  @Test
  public void deserializeStringOnlySelfClosing() throws Exception {
    parser.encode(toEncodeOnlySelf);
    String exp = parser.serializeTagsString();
    String obs = HTMLSearchableCompression.deserializeString(exp).serializeTagsString();
    assertEquals("Deserialize : ", exp, obs);
  }

  @Test
  public void encodeWithUrl() throws Exception {
    String toEncode =
      "<a href=\"https://www.franceinter.fr\" target=\"_blank\">https://www.franceinter.fr</a>";
    String plainText = "https://www.franceinter.fr";
    TagInstance anchor = new TagInstance("<a>", 0, 26);
    anchor.addAttribute(new Attribute("href", "https://www.franceinter.fr"));
    anchor.addAttribute(new Attribute("target", "_blank"));
    Deque<TagInstance> tags = new LinkedList<>();
    tags.push(anchor);
    parser.encode(toEncode);
    assertEquals("Encoding with URL - plain text : ", plainText, parser.getPlainText());
    assertEquals("Encoding with URL - tags : ", tags, parser.getTags());
  }

  @Test
  public void decodeWithUrl() throws Exception {
    String toEncode =
      "<a href=\"https://www.franceinter.fr\" target=\"_blank\">https://www.franceinter.fr</a>";
    String plainText = "https://www.franceinter.fr";
    TagInstance anchor = new TagInstance("<a>", 0, 26);
    anchor.addAttribute(new Attribute("href", "https://www.franceinter.fr"));
    anchor.addAttribute(new Attribute("target", "_blank"));
    Deque<TagInstance> tags = new LinkedList<>();
    tags.push(anchor);
    String observed = HTMLSearchableCompression.decode(plainText, tags, new LinkedList<>());
    assertEquals("Decode with url : ", toEncode, observed);
  }

  @Test
  public void serializeStringWithUrl() throws Exception {
    String toEncode =
      "<a href=\"https://www.franceinter.fr\" target=\"_blank\">https://www.franceinter.fr</a>";
    String plainText = "https://www.franceinter.fr";
    String expected =
      "#tags##taga;0;26#attrhref=\"https://www.franceinter.fr\";target=\"_blank\"#tags#";
    parser.encode(toEncode);
    assertEquals("Serialize with URL : ", expected, parser.serializeTagsString());
  }

  @Test
  public void deserializeStringWithUrl() throws Exception {
    String toEncode =
      "<a href=\"https://www.franceinter.fr\" target=\"_blank\">https://www.franceinter.fr</a>";
    parser.encode(toEncode);
    String exp = parser.serializeTagsString();
    String obs = HTMLSearchableCompression.deserializeString(exp).serializeTagsString();
    assertEquals("Deserialize with URL : ", exp, obs);
  }

  @Test
  public void richHtmlMailFromOutlook() throws Exception {

    // Load sample file
    StringBuilder sb = new StringBuilder();
    String line = null;
    FileInputStream fn = new FileInputStream("testRealHtml.html");
    InputStreamReader inReader = new InputStreamReader(fn, Charset.forName("UTF-8"));
    BufferedReader bReader = new BufferedReader(inReader);
    while ((line = bReader.readLine()) != null) {
      sb.append(line);
    }

    // If last ';' is missing in style attribut of original, it is added when decompressed
    String sFirst = "style=\"font-size:11\"";
    int first = sb.indexOf(sFirst);
    sb.insert(first + sFirst.length() - 1, ';');
    String sSecond = "style=\"font-size:10\"";
    int second = sb.indexOf(sSecond);
    sb.insert(second + sSecond.length() - 1, ';');
    String sThird = "style=\"font-size:7; color:#404040\"";
    int third = sb.indexOf(sThird);
    sb.insert(third + sThird.length() - 1, ';');
    String sThirdBis = "style=\"font-size:7; ";

    // Space within style attributes are deleted by compression/decompression
    int thirdBis = sb.indexOf(sThirdBis);
    sb.deleteCharAt(thirdBis + sThirdBis.length() - 1);

    HTMLSearchableCompression comp = new HTMLSearchableCompression();
    comp.encode(sb.toString());
    String tags = comp.serializeTagsString();
    String plain = comp.getPlainText();
    String result = HTMLSearchableCompression.decode(plain, tags);
    assertEquals("Encoding/decoding more complex html", sb.toString(), result);
  }

  @Test
  public void richSNCFMailFromOutlook() throws Exception {

    // Load sample file
    StringBuilder sb = new StringBuilder();
    String line = null;
    FileInputStream fn = new FileInputStream("testSNCFHtml.html");
    InputStreamReader inReader = new InputStreamReader(fn, Charset.forName("UTF-8"));
    BufferedReader bReader = new BufferedReader(inReader);
    while ((line = bReader.readLine()) != null) {
      sb.append(line);
    }

    HTMLSearchableCompression comp = new HTMLSearchableCompression();
    comp.encode(sb.toString());
    String tags = comp.serializeTagsString();
    String plain = comp.getPlainText();
    String result = HTMLSearchableCompression.decode(plain, tags);
    assertEquals("Encoding/decoding more complex html", sb.toString(), result);
  }

  @Test
  public void sncfDeserializeBugMailFromOutlook() throws Exception {

    // Load sample file
    StringBuilder sb = new StringBuilder();
    String line = null;
    FileInputStream fn = new FileInputStream("testSNCFDeserBug.html");
    InputStreamReader inReader = new InputStreamReader(fn, Charset.forName("UTF-8"));
    BufferedReader bReader = new BufferedReader(inReader);
    while ((line = bReader.readLine()) != null) {
      sb.append(line);
    }

    HTMLSearchableCompression comp = new HTMLSearchableCompression();
    comp.encode(sb.toString());
    String tags = comp.serializeTagsString();
    String plain = comp.getPlainText();
    String result = HTMLSearchableCompression.decode(plain, tags);
    System.out.println(result);
  }
}
