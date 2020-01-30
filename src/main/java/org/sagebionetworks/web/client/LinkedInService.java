package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.shared.LinkedInInfo;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("linkedin")
public interface LinkedInService extends RemoteService {

	public LinkedInInfo returnAuthUrl(String returnUrl);

	/**
	 * Create a UserProfile using data provided by LinkedIn.
	 * 
	 * @param requestToken
	 * @param secret
	 * @param verifier
	 * @param callbackUrl
	 * @return
	 */
	public UserProfile getCurrentUserInfo(String requestToken, String secret, String verifier, String callbackUrl);

}
