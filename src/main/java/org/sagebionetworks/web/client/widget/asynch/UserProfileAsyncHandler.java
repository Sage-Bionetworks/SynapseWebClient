package org.sagebionetworks.web.client.widget.asynch;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.sagebionetworks.repo.model.UserProfile;

public interface UserProfileAsyncHandler {
  void getUserProfile(String userId, AsyncCallback<UserProfile> callback);
}
