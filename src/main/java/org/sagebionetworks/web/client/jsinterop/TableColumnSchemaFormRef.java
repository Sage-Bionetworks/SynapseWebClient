package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class TableColumnSchemaFormRef {

  /**
   * The TableColumnSchemaForm component exposes an imperative handle to get the current state of the form's ColumnModels
   *
   * @return JSON representations of ColumnModel objects
   */
  public native Object[] getEditedColumnModels();

  /**
   * Returns true iff the form is valid
   */
  public native boolean validate();
}
