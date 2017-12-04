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
	
	public void initialize(String userId) {
		_initialize(userId);
	}

	private static native void _initialize(String userId) /*-{
		$wnd.amplitude.getInstance().init("fb6f2a76ea88503e61e8de28b2c4c22c", userId, {includeReferrer: true, includeUtm: true});
	}-*/;
	
}
