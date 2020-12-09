package org.sagebionetworks.web.client.jsinterop;
import com.google.gwt.dom.client.Element;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class ReactDOM {
	public static native ReactElement render(ReactElement element, Element container);

	public static native boolean unmountComponentAtNode(Element container);
}
