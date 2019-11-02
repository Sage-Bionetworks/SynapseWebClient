package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.UserProfile;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface UserProfileAsyncHandler {
	void getUserProfile(String userId, AsyncCallback<UserProfile> callback);
}
