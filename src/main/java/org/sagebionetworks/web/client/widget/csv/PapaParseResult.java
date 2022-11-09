package org.sagebionetworks.web.client.widget.csv;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * The results of a PapaParseWrapper.parse() call in JS returns a simple JS Object.
 * This class helps cast it back into a Java Object
 *
 * Structure of object:
 * https://www.papaparse.com/docs#results
 *
 *
 * Casting a loose Object to Java:
 * https://stackoverflow.com/questions/57852600/how-to-cast-a-return-value-from-javascript-to-java-in-gwt-jsni
 *
 *
 */
@JsType(isNative = true, name = "Object", namespace = JsPackage.GLOBAL)
public class PapaParseResult {

  //currently the only field we need
  public String[][] data;
}
