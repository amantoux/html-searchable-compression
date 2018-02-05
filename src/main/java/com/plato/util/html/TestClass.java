package com.plato.util.html;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * Created by Alan Mantoux.
 */
public class TestClass {

  public static void main(String[] args) {
    regexColon();
    realHtml();
  }

  static void regexColon() {
    String in = "\"text/html; charset=utf-8\"";
    String in3 = "\"text/html\"; \"charset=utf-8\"";
    String regex = "(?:(?<!\"));(?!(.)*\")";
    String colonExInQuotes = HTMLSearchableCompression.notBewteenQuotesRegex(";");
    Pattern p = Pattern.compile(colonExInQuotes);

    String[] inA = in.split(colonExInQuotes);
    System.out.println(inA[0]);

    String[] inA3 = in3.split(colonExInQuotes);
    System.out.println(inA3[0] + "," + inA3[1]);
  }

  static void realHtml() {
    StringBuilder sb = new StringBuilder();
    String line = null;

    try (FileInputStream fn = new FileInputStream("testRealHtml.html");
      InputStreamReader inReader = new InputStreamReader(fn, Charset.forName("UTF-8"));
      BufferedReader bReader = new BufferedReader(inReader)) {

      while ((line = bReader.readLine()) != null) {
        sb.append(line);
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    System.out.println(sb);

    HTMLSearchableCompression comp = new HTMLSearchableCompression();
    comp.encode(sb.toString());
    String tags = comp.serializeTagsString();
    System.out.println(tags);
    String plain = comp.getPlainText();
    System.out.println(HTMLSearchableCompression.decode(plain, tags));
  }

}
