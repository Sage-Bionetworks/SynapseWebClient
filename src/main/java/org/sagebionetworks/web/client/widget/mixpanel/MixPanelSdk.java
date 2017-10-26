package org.sagebionetworks.web.client.widget.mixpanel;


import com.google.inject.Inject;

/**
 * Wrapper around the MixPanel library
 * @author jayhodgson
 *
 */
public class MixPanelSdk {
	String currentUserId = null;
	@Inject
	public MixPanelSdk() {
	}

	public void initialize(final String userId, final String email) {
		if (!userId.equals(currentUserId)) {
			_initialize(userId, email);
			currentUserId = userId;
		}
	}

	private static native void _initialize(
			String userId,
			String synapseEmail) /*-{
		$wnd.mixpanel.identify(userId);
	    $wnd.mixpanel.register({
	        "email": synapseEmail
    	});
	    $wnd.mixpanel.track_links("#rootPanel a", "click link", 
	    	{"referrer": $doc.referrer}
	    );
	}-*/;
}
