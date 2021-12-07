package org.sagebionetworks.web.client.widget;

import org.sagebionetworks.web.client.jsinterop.ReactDOM;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Utility methods that manage the React component lifecycle from GWT lifecycle methods
 */
public class ReactComponentLifecycleUtils {
	/**
	 * Should be used to override {@link Widget#onUnload()}
	 * @param e
	 */
	protected static void onUnload(Element e) {
		ReactDOM.unmountComponentAtNode(e);
	}

	/**
	 * Should be used to override {@link Panel#clear()}
	 * @param e
	 */
	public static void clear(Element e) {
		ReactDOM.unmountComponentAtNode(e);
	}
}
