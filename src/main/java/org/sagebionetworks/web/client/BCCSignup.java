package org.sagebionetworks.web.client;

import org.sagebionetworks.web.shared.BCCSignupProfile;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("bcc")
public interface BCCSignup extends RemoteService {
	public void sendSignupEmail(BCCSignupProfile profile);
}
