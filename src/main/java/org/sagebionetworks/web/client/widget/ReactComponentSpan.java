package org.sagebionetworks.web.client.widget;

import org.gwtbootstrap3.client.ui.html.Span;

/**
 * Automatically unmounts the ReactComponent (if any) inside this div when this container is detached/unloaded.
 */
public class ReactComponentSpan extends Span {
	@Override
	protected void onUnload() {
		ReactComponentLifecycleUtils.onUnload(this.getElement());
		super.onUnload();
	}

	@Override
	public void clear() {
		ReactComponentLifecycleUtils.clear(this.getElement());
		super.clear();
	}
}
