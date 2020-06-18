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
@JsType(isNative = true, name="Papa", namespace = JsPackage.GLOBAL)
public class PapaParseWrapper {
	//TODO: introduce a factory that will ensure that the WebResource is loaded/injected into the HTML before we can use it
	//TODO: somehow make this instanced instead of a static call?? or just wrap this class in a object?
	public static native PapaParseResult parse(String string, PapaParseConfig config);
}
