package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.web.client.utils.CallbackP;

public interface IsACTMemberAsyncHandler {
	/**
	 * Main call. Returns true if the current user is a member of the ACT, and we are currently showing
	 * ACT UI.
	 * 
	 * @param callback
	 */
	void isACTActionAvailable(CallbackP<Boolean> callback);

	/**
	 * In most cases, isACTActionsAvailable() should be used instead of this method.
	 * 
	 * @param callback
	 */
	void isACTMember(CallbackP<Boolean> callback);

	/**
	 * If the current user is a member of the ACT, should ACT UI be shown?
	 * 
	 * @param visible
	 */
	void setACTActionVisible(boolean visible);

	boolean isACTActionVisible();
}
