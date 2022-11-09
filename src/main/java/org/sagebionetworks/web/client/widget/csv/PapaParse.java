package org.sagebionetworks.web.client.widget.csv;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * Wrapper around the CSV parser js library, PapaParse
 *
 * @author jayhodgson
 *
 */
@JsType(isNative = true, name = "Papa", namespace = JsPackage.GLOBAL)
class PapaParse {

  public static native PapaParseResult parse(
    String string,
    PapaParseConfig config
  );
}
