package com.eagles.util.datastructures;

import java.util.Iterator;
import java.util.Scanner;

/**
 * Generic linked list With default config of JVM, it tends to be slow when number of elements is
 * greater than 1 650 000 Integers. Please adjust Java Heap to avoid FullGC. With Integer elements,
 * 10 000 000 elements are added in ~800ms
 *
 * @param <Item>
 * @author Alan Mantoux
 */
public class MyLinkedList<Item> implements Iterable<Item> {

  private Node first = null;
  private Node last  = null;
  private int  size  = 0;

  public void addLast(Item element) {
    if (!isEmpty()) {
      last.next = new Node(element, last, null);
      last = last.next;
    } else {
      addToEmptyList(element);
    }
    size++;
  }

  public void addFirst(Item element) {
    if (!isEmpty()) {
      first.previous = new Node(element, null, first);
      first = first.previous;
    } else {
      addToEmptyList(element);
    }
    size++;
  }

  private void addToEmptyList(Item element) {
    first = new Node(element, null, null);
    last = first;
  }

  public Item removeLast() {
    if (!isEmpty()) {
      Item removed = last.content;
      last = last.previous;
      if (last != null)
        last.next = null;
      size--;
      return removed;
    }
    return null;
  }

  public Item removeFirst() {
    if (!isEmpty()) {
      Item removed = first.content;
      first = first.next;
      if (first != null)
        first.previous = null;
      size--;
      return removed;
    }
    return null;
  }

  public boolean isEmpty() {
    return size == 0;
  }

  public int size() {
    return size;
  }

  public Iterator<Item> iterator() {
    return new ListIterator();
  }

  private class Node {

    Item content;
    Node next;
    Node previous;

    private Node(Item content, Node previous, Node next) {
      this.content = content;
      this.previous = previous;
      this.next = next;
    }

  }


  private class ListIterator implements Iterator<Item> {

    private Node currentNode;

    private ListIterator() {
      this.currentNode = first;
    }

    public boolean hasNext() {
      return (currentNode != null);
    }

    public Item next() {
      Item next = currentNode.content;
      currentNode = currentNode.next;
      return next;
    }

  }

  @SuppressWarnings("all")
  public static void main(String[] args) {
    Scanner s = new Scanner(System.in);
    System.out.println("Hit enter to shoot");
    s.nextLine();

    MyLinkedList<Integer> list = new MyLinkedList();

    // Add elements to a linked list
    int nbInteger = 10_000_000;
    int i = 0;
    System.out.println("Adding elements...");
    long start = System.currentTimeMillis();
    while (i < nbInteger) {
      list.addLast(i++);
    }
    long end = System.currentTimeMillis();
    System.out.println(end - start + "ms to add all elements \n");
    s.nextLine();

    // Iterate through the list
    System.out.println("Iterating over list...");
    start = System.currentTimeMillis();
    int count = 0;
    for (int j : list) {
      count++;
    }
    end = System.currentTimeMillis();
    System.out.println(end - start + "ms to iterate over list");
    System.out.println("Last element is:" + list.last.content + "\n");

    // Remove all elements of the list
    System.out.println("Removing elements...");
    start = System.currentTimeMillis();
    while (!list.isEmpty()) {
      list.removeLast();
    }
    end = System.currentTimeMillis();
    System.out.println(end - start + "ms to remove elements");
    System.out.println("Last element is " + list.last + " and getSize is " + list.size());
    s.nextLine();
  }

}
