package com.plato.util.html;

/**
 * Created by Alan Mantoux.
 */
public interface Attribute {

  String ATTR_REGEX =
    // attribute key
    "([a-z0-9]|-)*"
      // equals
      + "="
      // regular attribute (class or other)
      + "\"(.)*\"";

  String serializeString();
}
