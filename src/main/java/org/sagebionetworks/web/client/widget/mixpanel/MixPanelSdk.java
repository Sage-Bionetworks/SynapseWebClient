package org.sagebionetworks.web.client.widget.mixpanel;


import com.google.inject.Inject;

/**
 * Wrapper around the MixPanel library
 * @author jayhodgson
 *
 */
public class MixPanelSdk {
	String currentUser = null;
	@Inject
	public MixPanelSdk() {
	}

	public void initialize(final String userId, final String email) {
		if (!userId.equals(currentUser)) {
			_changeUser(userId, email);	
		}
	}

	public void trackClick(String description) {
		_track("click", description);
	}

	private static native void _changeUser(
			String userId,
			String synapseEmail) /*-{
		$wnd.mixpanel.identify(userId);
		$wnd.mixpanel.register({
			"email": synapseEmail
		});
    }-*/;

	private static native void _track(String eventType, String eventDescription) /*-{
		$wnd.mixpanel.track(eventType, {"description" : eventDescription});
	}-*/;
}
