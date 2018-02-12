package com.plato.util.html;

public class PlayGround {

  public static void main(String[] args) {
    runTest();
  }

  private static void runTest() {
    String init = "<td style=\"font-size:0px\">abc<br></td>";
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 1; i++) {
      sb.append(init);
    }
    String in = sb.toString();

    HTMLSearchableCompression parser = new HTMLSearchableCompression();
    parser.encode(in);
    String tags = parser.serializeTagsString();
    String plain = parser.getPlainText();
    HTMLSearchableCompression parser2 = HTMLSearchableCompression.deserializeString(tags);
    System.out.println(HTMLSearchableCompression.decode(plain, parser2.getTags(), parser2.getSelfClosings()));
  }
}
