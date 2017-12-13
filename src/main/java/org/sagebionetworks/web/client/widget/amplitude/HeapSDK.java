package org.sagebionetworks.web.client.widget.amplitude;

import com.google.inject.Inject;

/**
 * Wrapper around the Heap analytics library
 * @author jayhodgson
 *
 */
public class HeapSDK {
	@Inject
	public HeapSDK() {
	}
	
	public void initialize(String userId) {
		_initialize(userId);
	}

	private static native void _initialize(String userId) /*-{
		try{
			$wnd.heap.identify(userId);
		} catch(err) {
			console.error(err);
		}
	}-*/;
	
}
