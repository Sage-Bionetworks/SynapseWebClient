package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class AccessRequirementAclEditorHandler {

  /**
   * The AccessRequirementAclEditor component exposes an imperative handle to
   * save the updated ACL.
   */
  public native void save();
}
