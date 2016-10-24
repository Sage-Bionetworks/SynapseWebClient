package org.sagebionetworks.web.client.widget.asynch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class UserProfileAsyncHandlerImpl implements UserProfileAsyncHandler {
	private Map<String, List<AsyncCallback<UserProfile>>> reference2Callback = new HashMap<String, List<AsyncCallback<UserProfile>>>();
	SynapseClientAsync synapseClient;
	// This singleton checks for new work every <DELAY> milliseconds.
	public static final int DELAY = 280;
	
	@Inject
	public UserProfileAsyncHandlerImpl(SynapseClientAsync synapseClient, GWTWrapper gwt) {
		this.synapseClient = synapseClient;
		Callback callback = new Callback() {
			@Override
			public void invoke() {
				executeRequests();
			}
		};
		gwt.scheduleFixedDelay(callback, DELAY);
	}
	
	@Override
	public void getUserProfile(String userId, AsyncCallback<UserProfile> callback) {
		List<AsyncCallback<UserProfile>> list = reference2Callback.get(userId);
		if (list == null) {
			list = new ArrayList<AsyncCallback<UserProfile>>();
			reference2Callback.put(userId, list);
		}
		list.add(callback);
	}
	
	public void executeRequests() {
		if (!reference2Callback.isEmpty()) {
			final Map<String, List<AsyncCallback<UserProfile>>> reference2CallbackCopy = new HashMap<String, List<AsyncCallback<UserProfile>>>();
			reference2CallbackCopy.putAll(reference2Callback);
			reference2Callback.clear();
			List<String> userIds = new ArrayList<String>();
			userIds.addAll(reference2CallbackCopy.keySet());
			synapseClient.listUserProfiles(userIds, new AsyncCallback<List<UserProfile>>() {
				@Override
				public void onFailure(Throwable caught) {
					// go through all requested objects, and inform them of the error
					for (String fileHandleId: reference2CallbackCopy.keySet()) {
						callOnFailure(fileHandleId, caught);
					}
				}
				
				private void callOnFailure(String userId, Throwable ex) {
					List<AsyncCallback<UserProfile>> callbacks = reference2CallbackCopy.get(userId);
					if (callbacks != null) {
						for (AsyncCallback<UserProfile> callback : callbacks) {
							callback.onFailure(ex);	
						}
					}
				}
				
				public void onSuccess(List<UserProfile> results) {
					// go through all results, and inform the proper callback of the success
					for (UserProfile result : results) {
						List<AsyncCallback<UserProfile>> callbacks = reference2CallbackCopy.remove(result.getOwnerId());
						if (callbacks != null) {
							for (AsyncCallback<UserProfile> callback : callbacks) {
								callback.onSuccess(result);	
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
