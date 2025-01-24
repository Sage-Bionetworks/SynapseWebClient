package org.sagebionetworks.web.client.jsinterop;

import elemental2.dom.FileList;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EntityUploadHandle {

  /**
   * The EntityUploadModal component exposes an imperative handle to programmatically upload files.
   */
  public native void handleUploads(FileList fileList);
}
