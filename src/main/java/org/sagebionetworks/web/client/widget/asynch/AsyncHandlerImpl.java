package org.sagebionetworks.web.client.widget.asynch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public abstract class AsyncHandlerImpl {
	private Map<String, List<AsyncCallback>> reference2Callback = new HashMap<String, List<AsyncCallback>>();

	public abstract void doCall(List<String> ids, AsyncCallback<List> callback);

	public abstract String getId(Object singleItem);

	@Inject
	public AsyncHandlerImpl(GWTWrapper gwt) {
		Callback callback = new Callback() {
			@Override
			public void invoke() {
				executeRequests();
			}
		};

		gwt.scheduleFixedDelay(callback, 200 + gwt.nextInt(150));
	}

	public void get(String id, AsyncCallback callback) {
		List<AsyncCallback> list = reference2Callback.get(id);
		if (list == null) {
			list = new ArrayList<AsyncCallback>();
			reference2Callback.put(id, list);
		}
		list.add(callback);
	}

	public void executeRequests() {
		if (!reference2Callback.isEmpty()) {
			final Map<String, List<AsyncCallback>> reference2CallbackCopy = new HashMap<String, List<AsyncCallback>>();
			reference2CallbackCopy.putAll(reference2Callback);
			reference2Callback.clear();
			List<String> ids = new ArrayList<String>();
			ids.addAll(reference2CallbackCopy.keySet());
			doCall(ids, new AsyncCallback<List>() {
				@Override
				public void onFailure(Throwable caught) {
					// go through all requested objects, and inform them of the error
					for (String fileHandleId : reference2CallbackCopy.keySet()) {
						callOnFailure(fileHandleId, caught);
					}
				}

				private void callOnFailure(String id, Throwable ex) {
					List<AsyncCallback> callbacks = reference2CallbackCopy.get(id);
					if (callbacks != null) {
						for (AsyncCallback callback : callbacks) {
							callback.onFailure(ex);
						}
					}
				}

				public void onSuccess(List results) {
					// go through all results, and inform the proper callback of the success
					for (Object result : results) {
						List<AsyncCallback> callbacks = reference2CallbackCopy.remove(getId(result));
						if (callbacks != null) {
							for (AsyncCallback callback : callbacks) {
								callback.onSuccess(result);
							}
						}
					}
					NotFoundException notReturnedException = new NotFoundException(DisplayConstants.ERROR_LOADING);
					for (String userId : reference2CallbackCopy.keySet()) {
						// not returned
						callOnFailure(userId, notReturnedException);
					}
				};
			});
		}
	}


}
