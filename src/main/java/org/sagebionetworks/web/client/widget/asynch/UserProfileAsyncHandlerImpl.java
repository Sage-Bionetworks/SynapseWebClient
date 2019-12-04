package org.sagebionetworks.web.client.widget.asynch;

import java.util.List;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class UserProfileAsyncHandlerImpl extends AsyncHandlerImpl implements UserProfileAsyncHandler {
	SynapseJavascriptClient jsClient;

	@Inject
	public UserProfileAsyncHandlerImpl(SynapseJavascriptClient jsClient, GWTWrapper gwt) {
		super(gwt);
		this.jsClient = jsClient;
	}

	@Override
	public void getUserProfile(String userId, AsyncCallback<UserProfile> callback) {
		super.get(userId, callback);
	}

	@Override
	public void doCall(List ids, AsyncCallback<List> callback) {
		jsClient.listUserProfiles(ids, (AsyncCallback<List>) callback);
	}

	@Override
	public String getId(Object singleItem) {
		return ((UserProfile) singleItem).getOwnerId();
	}
}
