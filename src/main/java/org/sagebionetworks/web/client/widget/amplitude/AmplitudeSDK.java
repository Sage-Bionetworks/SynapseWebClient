package org.sagebionetworks.web.client.widget.amplitude;

import com.google.inject.Inject;

/**
 * Wrapper around the Amplitude library
 * @author jayhodgson
 *
 */
public class AmplitudeSDK {
	@Inject
	public AmplitudeSDK() {
	}

	public void trackClick(String description) {
		_track(description);
	}

	private static native void _track(String description) /*-{
		$wnd.amplitude.getInstance().logEvent(description);
		
	}-*/;
}
