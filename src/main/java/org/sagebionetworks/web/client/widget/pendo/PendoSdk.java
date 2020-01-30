package org.sagebionetworks.web.client.widget.pendo;

import org.sagebionetworks.web.client.GWTWrapper;
import com.google.inject.Inject;

/**
 * Wrapper around the Pendo library
 * 
 * @author jayhodgson
 *
 */
public class PendoSdk {
	GWTWrapper gwt;
	String userId, email;

	@Inject
	public PendoSdk(GWTWrapper gwt) {
		this.gwt = gwt;
	}

	public void initialize(String userId, String email) {
		if (userId.equals(this.userId) && email.equals(this.email)) {
			return;
		}
		if (_isLoaded()) {
			this.userId = userId;
			this.email = email;
			_initialize(userId, email);
		} else {
			gwt.scheduleExecution(() -> {
				initialize(userId, email);
			}, 200);
		}
	}

	private static native boolean _isLoaded() /*-{
		return $wnd.pendo;
	}-*/;

	private static native void _initialize(String userId, String synapseEmail) /*-{
		// Call this whenever information about your visitors becomes available
		// Please use Strings, Numbers, or Bools for value types.
		try {
			$wnd.pendo.initialize({
				sanitizeUrl : function(url) {
					// NOTE: use pendo.normalizedUrl in the js console to see what url we send to Pendo for the page that you're on!
					//					console.log('pendo given this url:' + url);
					if (url.includes('#!')) {
						// also replace last ':' (place token separator) with a '/'
						var n = url.lastIndexOf(':');
						var pendoSanitizedUrl = (url.slice(0, n) + '/' + url
								.slice(n + 1)).replace('#!', '');
						//						console.log('pendo sanitized url:' + pendoSanitizedUrl);
						return pendoSanitizedUrl;
					} else {
						// no place like Home
						var sep = url.endsWith('/') ? '' : '/';
						return url + sep + 'Home';
					}
				},
				visitor : {
					id : userId, // Required if user is logged in
					email : synapseEmail
				// Optional
				// role:         // Optional

				// You can add any additional visitor level key-values here,
				// as long as it's not one of the above reserved names.
				},

				account : {
				// id:           'ACCOUNT-UNIQUE-ID' // Highly recommended
				// name:         // Optional
				// planLevel:    // Optional
				// planPrice:    // Optional
				// creationDate: // Optional

				// You can add any additional account level key-values here,
				// as long as it's not one of the above reserved names.
				},
				excludeAllText : true
			// Do not send DOM element text to Pendo
			});
		} catch (err) {
			console.error(err);
		}
	}-*/;
}
