package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * JsInterop bridge for the ColumnModel JavaScript object expected by the React CsvPreview component
 * Only includes fields needed by GWT, add more as needed.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ColumnModelJsObject {

  public String id;
  public String name;
  public String columnType; // e.g. "STRING", "BOOLEAN", etc.
  public Double maximumSize;
}
