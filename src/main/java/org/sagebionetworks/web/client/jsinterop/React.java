package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class React {

	public static native <P extends ReactComponentProps> ReactElement createElement(ReactFunctionComponent<P> component, P props);
}
