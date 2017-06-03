package com.eagles.util.datastructures;

/**
 * Created by Alan Mantoux.
 */
public class InsertStringBuilder {

  private char[] value;
  private int size = 0;

  public InsertStringBuilder() {
    value = new char[16];
  }

  public InsertStringBuilder(int capacity) {
    if (capacity > 0)
      value = new char[capacity];
    else
      value = new char[16];
  }

  /*
    Position of the next character TO BE filled
   */
  private int offset() {
    return value.length - size;
  }

  public InsertStringBuilder insertFirst(String str) {
    if (str == null)
      return this;
    char[] strChars = str.toCharArray();
    int len = str.length();
    ensureCapacityInternal(size + len);
    size += len;
    System.arraycopy(strChars, 0, value, offset(), len);
    return this;
  }

  private void ensureCapacityInternal(int minimumCapacity) {
    // overflow-conscious code
    if (minimumCapacity - value.length > 0)
      expandCapacity(minimumCapacity);
  }

  /**
   * This implements the expansion semantics of ensureCapacity with no
   * size check or synchronization.
   */
  private void expandCapacity(int minimumCapacity) {
    int newCapacity = value.length * 2 + 2;
    if (newCapacity - minimumCapacity < 0)
      newCapacity = minimumCapacity;
    if (newCapacity < 0) {
      if (minimumCapacity < 0) // overflow
        throw new OutOfMemoryError();
      newCapacity = Integer.MAX_VALUE;
    }
    char[] copy = new char[newCapacity];
    System.arraycopy(value, offset(), copy, newCapacity - size, size);
    value = copy;
  }

  public String toString() {
    return new String(value, offset(), size);
  }
}
