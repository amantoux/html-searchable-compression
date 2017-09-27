package com.plato.util.html;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Alan Mantoux.
 */
public class TestClass {
  public static void main(String[] args) {
    String s = "<p class=\"ql-font-style\" style=\"font-color:red;font-size:10em;\">";
    String s1 =
      "<strong class=\"ql-font-style\">REALLY</strong><p class=\"ql-font-style\" style=\"font-color:red;font-size:10em;\">sdf</p>";
    StringBuilder sbRegex = new StringBuilder();
    sbRegex.append("</?(");
    for (Tag t : Tag.values()) {
      sbRegex.append(t.string).append("|");
    }
    sbRegex.deleteCharAt(sbRegex.length() - 1);
    sbRegex.append(")(\\s(class|style)=\"[^\"]+\")*");
    sbRegex.append(">");
    String sRegex = sbRegex.toString();
    Pattern p = Pattern.compile(sRegex);
    Matcher m = p.matcher(s);
    System.out.println(m.find());
    m = p.matcher(s1);
    System.out.println(m.find());
    System.out.println(m.find());
    System.out.println(m.find());
    System.out.println(m.find());
  }
}
