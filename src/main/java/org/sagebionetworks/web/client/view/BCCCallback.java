package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.shared.BCCSignupProfile;


/**
 * 
 * This interface provides a callback to be invoked after the user agrees to the Terms of user
 * 
 * @author brucehoff
 *
 */
public interface BCCCallback {
	void submit(BCCSignupProfile profile);
}
