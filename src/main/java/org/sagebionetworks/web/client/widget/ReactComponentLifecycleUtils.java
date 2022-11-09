package org.sagebionetworks.web.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;
import org.sagebionetworks.web.client.jsinterop.ReactDOM;
import org.sagebionetworks.web.client.jsinterop.ReactDOMRoot;

/**
 * Utility methods that manage the React component lifecycle from GWT lifecycle methods
 */
public class ReactComponentLifecycleUtils {

  /**
   * Should be used to override {@link Widget#onLoad()} ()}
   * The calling class should store an instance variable for the returned root.
   * @param e
   * @return
   */
  protected static ReactDOMRoot onLoad(Element e) {
    return ReactDOM.createRoot(e);
  }

  /**
   * Should be used to override {@link Widget#onUnload()}
   * @param e
   */
  protected static void onUnload(ReactDOMRoot root) {
    if (root != null) {
      root.unmount();
    }
  }
}
