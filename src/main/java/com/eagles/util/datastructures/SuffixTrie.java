package com.eagles.util.datastructures;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class SuffixTrie<V> {

  private final AccumTST<V> tst;

  public SuffixTrie() {
    super();
    tst = new AccumTST<>();
  }

  public static void main(String[] args)
    throws IllegalArgumentException, IllegalAccessException, IOException {
    SuffixTrie<Integer> st = new SuffixTrie<>();
    Scanner sc = null;

    long start = System.currentTimeMillis();
    try {
      sc = new Scanner(new File("dictionary-yawl.txt"), "UTF-8");
      while (sc.hasNextLine()) {
        String s = sc.nextLine();
        st.put(s, 0);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } finally {
      if (sc != null)
        sc.close();
    }
    long end = System.currentTimeMillis();
    System.out.println(end - start + " ms to build suffix tree");

    start = System.currentTimeMillis();
    Iterable<Integer> res = st.get("ABACT");
    end = System.currentTimeMillis();
    System.out.println(end - start + " ms to getParam all matches");
    int i = 0;
    for (int integer : res) {
      System.out.println(integer);
      i++;
    }
    System.out.println(i + " words match");
  }

  public void put(String key, V val) {
    String[] suffices = buildSuffices(key);
    for (int i = 0; i < suffices.length - 1; i++) {
      tst.put(suffices[i], val);
    }
  }

  private String[] buildSuffices(String text) {
    String[] res = new String[text.length() + 1];
    StringBuilder sb = new StringBuilder(text);
    res[0] = sb.toString();
    int index = 1;
    while (sb.length() > 0) {
      res[index++] = sb.deleteCharAt(0).toString();
    }
    return res;
  }

  public Iterable<V> get(String query) {
    return tst.valuesWithPrefix(query);
  }
}
