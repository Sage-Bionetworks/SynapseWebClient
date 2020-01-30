package org.sagebionetworks.web.client.widget.lazyload;

import org.sagebionetworks.web.client.utils.Callback;

public interface SupportsLazyLoadInterface {
	boolean isInViewport();

	boolean isAttached();

	void setOnAttachCallback(Callback onAttachCallback);
}
