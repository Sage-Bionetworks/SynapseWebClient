package org.sagebionetworks.web.client.widget;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.jsinterop.ReactDOM;

/**
 * Automatically unmounts the ReactComponent (if any) inside this div when this container is detached/unloaded.
 */
public class ReactComponentDiv extends Div {
	@Override
	protected void onUnload() {
		ReactDOM.unmountComponentAtNode(this.getElement());
		super.onUnload();
	}
}
