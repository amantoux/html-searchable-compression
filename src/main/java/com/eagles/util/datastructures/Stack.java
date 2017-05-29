package com.eagles.util.datastructures;

import java.util.Iterator;
import java.util.Scanner;

/**
 * @param <ITEM>
 * @author Alan Mantoux
 */
public class Stack<ITEM> implements Iterable<ITEM> {

  private Node first;
  private int  size;

  public Stack() {
    first = null;
    size = 0;
  }

  public void push(ITEM element) {
    if (!isEmpty()) {
      Node oldFirst = first;
      first = new Node(element, oldFirst);
    } else {
      addToEmptyList(element);
    }
    size++;
  }

  private void addToEmptyList(ITEM element) {
    first = new Node(element, null);
  }

  public ITEM pop() {
    if (!isEmpty()) {
      ITEM popped = first.content;
      first = first.next;
      size--;
      return popped;
    } else {
      return null;
    }
  }

  public ITEM peek() {
    if (first == null)
      return null;
    return first.content;
  }

  public boolean isEmpty() {
    return size == 0;
  }

  public int size() {
    return size;
  }

  @Override
  public String toString() {
    StringBuilder stackString = new StringBuilder();
    Iterator<ITEM> it = this.iterator();
    stackString.append("Stack elements:\n");
    while(it.hasNext()) {
      stackString.append(it.next().toString());
      stackString.append("\n");
    }
    return stackString.toString();
  }

  @Override
  public Iterator<ITEM> iterator() {
    return new StackIterator();
  }

  private class Node {

    ITEM content;
    Node next;

    private Node(ITEM content, Node next) {
      this.content = content;
      this.next = next;
    }

  }


  private class StackIterator implements Iterator<ITEM> {

    private Node currentNode = first;

    @Override
    public boolean hasNext() {
      return currentNode != null;
    }

    @Override
    public ITEM next() {
      ITEM next = currentNode.content;
      currentNode = currentNode.next;
      return next;
    }

  }

  @SuppressWarnings("all")
  public static void main(String[] args) {
    Scanner s = new Scanner(System.in);
    System.out.println("Hit enter to shoot!");
    s.nextLine();

    Stack<Integer> stack = new Stack<>();

    // enqueue all elements
    int nbInteger = 10_000_000;
    int i = 0;
    System.out.println("Pushing elements...");
    long start = System.currentTimeMillis();
    while (i < nbInteger) {
      stack.push(i++);
    }
    long end = System.currentTimeMillis();
    System.out.println(end - start + "ms to enqueue\n");

    // iterate over queue
    System.out.println("Iterating over queue");
    int count = 0;
    start = System.currentTimeMillis();
    for (int j : stack) {
      count++;
      // System.out.println(j + " ");
    }
    end = System.currentTimeMillis();
    System.out.println(end - start + "ms to iterate\n");

    // dequeue all elements
    System.out.println("Poping all elements");
    start = System.currentTimeMillis();
    while (!stack.isEmpty()) {
      stack.pop();
    }
    end = System.currentTimeMillis();
    System.out.println(end - start + "ms to dequeue");
    System.out.println("Size is " + stack.size());
  }

}
