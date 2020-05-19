package com.plato.util.html;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Alan Mantoux.
 */
public class TestPerformanceClass {

  public static final int NB_REPET = 1_000;

  public static void main(String[] args) {
    System.out.println("Ready?");
    Scanner sc = new Scanner(System.in);
    sc.nextLine();
    executeTest();
  }

  private static String realFile() {
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

    return sb.toString();
  }

  private static long computeSize(HTMLSearchableCompression parser) {
    long size = parser.getPlainText().length() * (long) 16;
    size += parser.serializeTagsString().length() * (long) 16;
    return size;
  }

  private static void executeTest() {
    HTMLSearchableCompression parser = new HTMLSearchableCompression();

    long start, end;
    String seed = realFile();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < NB_REPET; i++) {
      sb.append(seed);
    }
    System.out.println("Init string length : " + seed.length());
    System.out.println("Number of repetitions : " + NB_REPET);
    System.out.println(
      "Estimated size of file : " + seed.length() * NB_REPET * 16 /* char size*/ / 1024 / 1024
        + "Mo");
    String toEncode = sb.toString();

    start = System.currentTimeMillis();
    parser.encode(toEncode);
    end = System.currentTimeMillis();

    long sizeInit = (long) (toEncode.length()) * 16;
    long sizeEnd = computeSize(parser);
    double ratio = sizeEnd * 100. / sizeInit;

    printResults(parser, toEncode, start, end, sizeInit, sizeEnd, ratio);
  }

  private static void printResults(HTMLSearchableCompression parser,
                                   String toEncode,
                                   long start,
                                   long end,
                                   long sizeInit,
                                   long sizeEnd,
                                   double ratio) {
    DecimalFormat myFormatter = new DecimalFormat("###,###,###");
    String sSizeInit = myFormatter.format(sizeInit);
    String sSizeEnd = myFormatter.format(sizeEnd);
    System.out.printf("Compression ratio : %2.2f%%%n", ratio);
    System.out.println("Start size : " + sSizeInit + " bits");
    System.out.println("End size : " + sSizeEnd + " bits\n");

    System.out.println("---- encoding ----");
    System.out.println("Encoded in " + (end - start) + "ms");
    System.out.println("");

    start = System.currentTimeMillis();
    String decoded = HTMLSearchableCompression.decode(parser.getPlainText(),
                                                      parser.getTags(),
                                                      parser.getSelfClosings());
    end = System.currentTimeMillis();

    List<String> original = Collections.singletonList(toEncode);
    List<String> revised = Collections.singletonList(decoded);

    Patch patch = DiffUtils.diff(original, revised);
    List<Delta> deltas = patch.getDeltas();
    for (Delta delta : deltas) {
      //      System.out.println(delta);
    }

    System.out.println("---- decoding ----");
    System.out.println("Decoded in " + (end - start) + "ms");
    System.out.println("");

    parser.encode(toEncode);
    start = System.currentTimeMillis();
    String serialString = parser.serializeTagsString();
    end = System.currentTimeMillis();

    System.out.println("---- Serializing ---");
    System.out.println("Serialize in " + (end - start) + "ms");
    System.out.println("");

    start = System.currentTimeMillis();
    HTMLSearchableCompression.deserializeString(serialString);
    end = System.currentTimeMillis();

    System.out.println("---- Deserializing ---");
    System.out.println("Deserialize in " + (end - start) + "ms");
    System.out.println("");
  }

}
