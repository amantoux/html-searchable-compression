package com.plato.util.html;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Alan Mantoux.
 */
public class TagInstanceTest {
  public static String serialString = "p;0;5#stylecolor#:red;background-color#:red#classql-editor";
  private static TagInstance tag;

  @BeforeClass
  public static void init() {
    tag = new TagInstance("<p>", 0, 5);
    tag.addStyleAttribute(new StyleAttribute("color", "red"));
    tag.addStyleAttribute(new StyleAttribute("background-color", "red"));
    tag.addClassAttribute(new ClassAttribute("ql-editor"));
  }

  @Test
  public void serializeString() throws Exception {
    assertEquals("Serialize : ", serialString, tag.serializeString());
  }

  @Test
  public void deserializeString() throws Exception {
    String sTag = tag.serializeString();
    assertEquals("Deserialize : ", tag, TagInstance.deserializeString(sTag, false));
  }

}
