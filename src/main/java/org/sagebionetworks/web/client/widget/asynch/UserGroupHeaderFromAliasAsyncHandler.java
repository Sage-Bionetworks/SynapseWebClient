package org.sagebionetworks.web.client.widget.asynch;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.sagebionetworks.repo.model.UserGroupHeader;

public interface UserGroupHeaderFromAliasAsyncHandler {
  void getUserGroupHeader(
    String alias,
    AsyncCallback<UserGroupHeader> callback
  );
}
