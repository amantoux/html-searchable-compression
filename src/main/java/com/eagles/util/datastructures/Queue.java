package com.eagles.util.datastructures;

import java.util.Iterator;
import java.util.Scanner;

/**
 * @param <Item>
 * @author Alan Mantoux
 */
public class Queue<Item> implements Iterable<Item> {

  private Node first = null;
  private Node last  = null;
  private int  size  = 0;

  public Iterator<Item> iterator() {
    return new QueueIterator();
  }


  private class Node {

    Item content;
    Node next;

    private Node(Item content, Node next) {
      this.content = content;
      this.next = next;
    }

  }


  private class QueueIterator implements Iterator<Item> {

    Node currentNode;

    private QueueIterator() {
      currentNode = first;
    }

    public boolean hasNext() {
      return currentNode != null;
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
    System.out.println("Hit enter to shoot!");
    s.nextLine();

    Queue<Integer> q = new Queue();

    // enqueue all elements
    int nbInteger = 10_000_000;
    int i = 0;
    System.out.println("Enqueuing elements...");
    long start = System.currentTimeMillis();
    while (i < nbInteger) {
      q.enqueue(i++);
    }
    long end = System.currentTimeMillis();
    System.out.println(end - start + "ms to enqueue\n");

    // iterate over queue
    System.out.println("Iterating over queue");
    int count = 0;
    start = System.currentTimeMillis();
    for (int j : q) {
      count++;
      // System.out.println(j + " ");
    }
    end = System.currentTimeMillis();
    System.out.println(end - start + "ms to iterate\n");

    // dequeue all elements
    System.out.println("Dequeuing all elements");
    start = System.currentTimeMillis();
    while (!q.isEmpty()) {
      q.dequeue();
    }
    end = System.currentTimeMillis();
    System.out.println(end - start + "ms to dequeue");
    System.out.println("Size is " + q.size());
  }

  public void enqueue(Item element) {
    if (!isEmpty()) {
      last.next = new Node(element, null);
      last = last.next;
    } else {
      addToEmptyList(element);
    }
    size++;
  }

  private void addToEmptyList(Item element) {
    first = new Node(element, null);
    last = first;
  }

  public boolean isEmpty() {
    return size == 0;
  }

  public Item dequeue() {
    if (!isEmpty()) {
      Item element = first.content;
      first = first.next;
      size--;
      return element;
    } else {
      return null;
    }
  }

  public int size() {
    return size;
  }

}
