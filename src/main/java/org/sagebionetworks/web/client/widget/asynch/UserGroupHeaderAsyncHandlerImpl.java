package org.sagebionetworks.web.client.widget.asynch;

import java.util.List;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class UserGroupHeaderAsyncHandlerImpl extends AsyncHandlerImpl implements UserGroupHeaderAsyncHandler {
	SynapseJavascriptClient jsClient;

	@Inject
	public UserGroupHeaderAsyncHandlerImpl(SynapseJavascriptClient jsClient, GWTWrapper gwt) {
		super(gwt);
		this.jsClient = jsClient;
	}

	@Override
	public String getId(Object singleItem) {
		return ((UserGroupHeader) singleItem).getOwnerId();
	}

	@Override
	public void getUserGroupHeader(String principalId, AsyncCallback<UserGroupHeader> callback) {
		super.get(principalId, callback);
	}

	@Override
	public void doCall(List ids, final AsyncCallback<List> callback) {
		jsClient.getUserGroupHeadersById(ids, new AsyncCallback<UserGroupHeaderResponsePage>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			@Override
			public void onSuccess(UserGroupHeaderResponsePage result) {
				callback.onSuccess(result.getChildren());
			}
		});
	}
}
