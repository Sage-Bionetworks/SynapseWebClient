package org.sagebionetworks.web.client;

import org.sagebionetworks.web.shared.BCCSignupProfile;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

public interface BCCSignupAsync {
	public void sendSignupEmail(BCCSignupProfile profile, AsyncCallback<Void> callback);
}
