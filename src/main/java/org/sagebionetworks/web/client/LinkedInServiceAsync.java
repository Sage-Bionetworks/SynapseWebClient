package org.sagebionetworks.web.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.shared.LinkedInInfo;

public interface LinkedInServiceAsync {
  void returnAuthUrl(String returnUrl, AsyncCallback<LinkedInInfo> callback);

  void getCurrentUserInfo(
    String requestToken,
    String secret,
    String verifier,
    String callbackUrl,
    AsyncCallback<UserProfile> callback
  );
}
