package org.sagebionetworks.web.client.widget.csv;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * Wrapper for a config file
 * https://www.papaparse.com/docs#config
 */
@JsType(isNative = true, name = "Object", namespace = JsPackage.GLOBAL)
class PapaParseConfig {

  @JsProperty
  public String delimiter;
}
