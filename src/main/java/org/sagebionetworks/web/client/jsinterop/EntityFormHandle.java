package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * Imperative handle exposed by the SRC EntityForm component.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EntityFormHandle {

  /**
   * Validates the form and, if valid, saves the user's answers. Does nothing until the form has loaded.
   */
  public native void submitForm();
}
