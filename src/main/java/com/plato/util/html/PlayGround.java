package com.plato.util.html;

public class PlayGround {

  public static void main(String[] args) {
    runTest();
  }

  private static void runTest() {
    String init = "<body><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
        "    <meta content=\"text/html; charset=utf-8\">" +
        "Essai</body>";
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 1_000_000; i++) {
      sb.append(init);
    }
    String in = sb.toString();

    HTMLSearchableCompression parser = new HTMLSearchableCompression();
    parser.encode(in);
    String tags = parser.serializeTagsString();
    String plain = parser.getPlainText();
    HTMLSearchableCompression parser2 = HTMLSearchableCompression.deserializeString(tags);
  }
}
