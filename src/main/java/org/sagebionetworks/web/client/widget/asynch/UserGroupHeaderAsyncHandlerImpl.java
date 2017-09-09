package org.sagebionetworks.web.client.widget.asynch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class UserGroupHeaderAsyncHandlerImpl implements UserGroupHeaderAsyncHandler {
	private Map<String, List<AsyncCallback<UserGroupHeader>>> reference2Callback = new HashMap<String, List<AsyncCallback<UserGroupHeader>>>();
	SynapseJavascriptClient jsClient;
	
	@Inject
	public UserGroupHeaderAsyncHandlerImpl(SynapseJavascriptClient jsClient, GWTWrapper gwt) {
		this.jsClient = jsClient;
		Callback callback = new Callback() {
			@Override
			public void invoke() {
				executeRequests();
			}
		};
		gwt.scheduleFixedDelay(callback, 200 + gwt.nextInt(150));
	}
	
	
	@Override
	public void getUserGroupHeader(String principalId, AsyncCallback<UserGroupHeader> callback) {
		List<AsyncCallback<UserGroupHeader>> list = reference2Callback.get(principalId);
		if (list == null) {
			list = new ArrayList<AsyncCallback<UserGroupHeader>>();
			reference2Callback.put(principalId, list);
		}
		list.add(callback);
	}
	
	public void executeRequests() {
		if (!reference2Callback.isEmpty()) {
			final Map<String, List<AsyncCallback<UserGroupHeader>>> reference2CallbackCopy = new HashMap<String, List<AsyncCallback<UserGroupHeader>>>();
			reference2CallbackCopy.putAll(reference2Callback);
			reference2Callback.clear();
			ArrayList<String> userIds = new ArrayList<String>();
			userIds.addAll(reference2CallbackCopy.keySet());
			jsClient.getUserGroupHeadersById(userIds, new AsyncCallback<UserGroupHeaderResponsePage>() {
				@Override
				public void onFailure(Throwable caught) {
					// go through all requested objects, and inform them of the error
					for (String fileHandleId: reference2CallbackCopy.keySet()) {
						callOnFailure(fileHandleId, caught);
					}
				}
				
				private void callOnFailure(String userId, Throwable ex) {
					List<AsyncCallback<UserGroupHeader>> callbacks = reference2CallbackCopy.get(userId);
					if (callbacks != null) {
						for (AsyncCallback<UserGroupHeader> callback : callbacks) {
							callback.onFailure(ex);	
						}
					}
				}
				
				public void onSuccess(UserGroupHeaderResponsePage results) {
					// go through all results, and inform the proper callback of the success
					for (UserGroupHeader header : results.getChildren()) {
						List<AsyncCallback<UserGroupHeader>> callbacks = reference2CallbackCopy.remove(header.getOwnerId());
						if (callbacks != null) {
							for (AsyncCallback<UserGroupHeader> callback : callbacks) {
								callback.onSuccess(header);
							}
						}
					}
					UnknownErrorException notReturnedException = new UnknownErrorException(DisplayConstants.ERROR_LOADING);
					for (String userId : reference2CallbackCopy.keySet()) {
						// not returned
						callOnFailure(userId, notReturnedException);
					}
				};
			});
		}
	}
	
	
}
