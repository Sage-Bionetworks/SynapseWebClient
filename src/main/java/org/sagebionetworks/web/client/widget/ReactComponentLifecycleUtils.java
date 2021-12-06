package org.sagebionetworks.web.client.widget;

import org.sagebionetworks.web.client.jsinterop.ReactDOM;

import com.google.gwt.dom.client.Element;

/**
 * Utility methods that manage the React component lifecycle from GWT lifecycle methods
 */
public abstract class ReactComponentLifecycleUtils {
	protected static void onUnload(Element e) {
		ReactDOM.unmountComponentAtNode(e);
	}

	public static void clear(Element e) {
		ReactDOM.unmountComponentAtNode(e);
	}
}
