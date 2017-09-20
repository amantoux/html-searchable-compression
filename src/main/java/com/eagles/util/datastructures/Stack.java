package com.eagles.util.datastructures;

import java.util.*;

/**
 * @param <I>
 * @author Alan Mantoux
 */
public class Stack<I> implements Iterable<I> {

  private Node first;
  private int  size;

  public Stack() {
    first = null;
    size = 0;
  }

  public I peek() {
    if (first == null)
      return null;
    return first.content;
  }

  @Override
  public String toString() {
    StringBuilder stackString = new StringBuilder();
    Iterator<I> it = this.iterator();
    stackString.append("Stack elements:\n");
    while (it.hasNext()) {
      stackString.append(it.next().toString());
      stackString.append("\n");
    }
    return stackString.deleteCharAt(stackString.length() - 1).toString();
  }

  @Override
  public Iterator<I> iterator() {
    return new StackIterator();
  }


  private class Node {

    I    content;
    Node next;

    private Node(I content, Node next) {
      this.content = content;
      this.next = next;
    }

  }


  private class StackIterator implements Iterator<I> {

    private Node currentNode = first;

    @Override
    public boolean hasNext() {
      return currentNode != null;
    }

    @Override
    public I next() {
      if (!hasNext())
        throw new NoSuchElementException();
      I next = currentNode.content;
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

  public void push(I element) {
    if (!isEmpty()) {
      Node oldFirst = first;
      first = new Node(element, oldFirst);
    } else {
      addToEmptyList(element);
    }
    size++;
  }

  private void addToEmptyList(I element) {
    first = new Node(element, null);
  }

  public boolean isEmpty() {
    return size == 0;
  }

  public I pop() {
    if (!isEmpty()) {
      I popped = first.content;
      first = first.next;
      size--;
      return popped;
    } else {
      return null;
    }
  }

  public int size() {
    return size;
  }

}
