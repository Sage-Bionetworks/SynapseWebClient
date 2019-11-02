package org.sagebionetworks.web.client.widget.upload;

import com.google.gwt.core.client.JavaScriptObject;

public class JavaScriptObjectWrapper {
	private JavaScriptObject jsObject;

	public JavaScriptObjectWrapper(JavaScriptObject jsObject) {
		this.jsObject = jsObject;
	}

	JavaScriptObject get() {
		return jsObject;
	}
}
