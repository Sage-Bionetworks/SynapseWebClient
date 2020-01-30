package org.sagebionetworks.web.client.widget.asynch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class UserGroupHeaderFromAliasAsyncHandlerImpl implements UserGroupHeaderFromAliasAsyncHandler {
	private Map<String, List<AsyncCallback<UserGroupHeader>>> reference2Callback = new HashMap<String, List<AsyncCallback<UserGroupHeader>>>();
	SynapseJavascriptClient jsClient;
	GWTWrapper gwt;

	@Inject
	public UserGroupHeaderFromAliasAsyncHandlerImpl(SynapseJavascriptClient jsClient, GWTWrapper gwt) {
		this.jsClient = jsClient;
		this.gwt = gwt;
		Callback callback = new Callback() {
			@Override
			public void invoke() {
				executeRequests();
			}
		};
		gwt.scheduleFixedDelay(callback, 200 + gwt.nextInt(150));
	}


	@Override
	public void getUserGroupHeader(String alias, AsyncCallback<UserGroupHeader> callback) {
		String key = gwt.getUniqueAliasName(alias).toLowerCase();
		List<AsyncCallback<UserGroupHeader>> list = reference2Callback.get(key);
		if (list == null) {
			list = new ArrayList<AsyncCallback<UserGroupHeader>>();
			reference2Callback.put(key, list);
		}
		list.add(callback);
	}

	public void executeRequests() {
		if (!reference2Callback.isEmpty()) {
			final Map<String, List<AsyncCallback<UserGroupHeader>>> reference2CallbackCopy = new HashMap<String, List<AsyncCallback<UserGroupHeader>>>();
			reference2CallbackCopy.putAll(reference2Callback);
			reference2Callback.clear();
			ArrayList<String> aliasNames = new ArrayList<String>();
			aliasNames.addAll(reference2CallbackCopy.keySet());
			jsClient.getUserGroupHeadersByAlias(aliasNames, new AsyncCallback<List<UserGroupHeader>>() {
				@Override
				public void onFailure(Throwable caught) {
					// go through all requested objects, and inform them of the error
					for (String fileHandleId : reference2CallbackCopy.keySet()) {
						callOnFailure(fileHandleId, caught);
					}
				}

				private void callOnFailure(String alias, Throwable ex) {
					List<AsyncCallback<UserGroupHeader>> callbacks = reference2CallbackCopy.get(alias);
					if (callbacks != null) {
						for (AsyncCallback<UserGroupHeader> callback : callbacks) {
							callback.onFailure(ex);
						}
					}
				}

				public void onSuccess(List<UserGroupHeader> results) {
					// go through all results, and inform the proper callback of the success
					for (UserGroupHeader header : results) {
						String alias = gwt.getUniqueAliasName(header.getUserName()).toLowerCase();
						List<AsyncCallback<UserGroupHeader>> callbacks = reference2CallbackCopy.remove(alias);
						if (callbacks != null) {
							for (AsyncCallback<UserGroupHeader> callback : callbacks) {
								callback.onSuccess(header);
							}
						}
					}
					NotFoundException notReturnedException = new NotFoundException(DisplayConstants.ERROR_LOADING);
					for (String alias : reference2CallbackCopy.keySet()) {
						// not returned
						callOnFailure(alias, notReturnedException);
					}
				};
			});
		}
	}


}
