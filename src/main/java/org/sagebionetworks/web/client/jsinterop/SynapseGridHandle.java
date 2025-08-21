package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class SynapseGridHandle {

  /**
   * The SynapseGrid component exposes an imperative handle to initialize the grid session
   */
  public native void initializeGrid();
}
