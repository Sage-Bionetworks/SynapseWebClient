package org.sagebionetworks.web.client.widget.asynch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class EntityHeaderAsyncHandlerImpl implements EntityHeaderAsyncHandler {
	private Map<String, List<AsyncCallback<EntityHeader>>> reference2Callback = new HashMap<String, List<AsyncCallback<EntityHeader>>>();
	SynapseClientAsync synapseClient;
	// This singleton checks for new work every <DELAY> milliseconds.
	public static final int DELAY = 300;
	
	@Inject
	public EntityHeaderAsyncHandlerImpl(SynapseClientAsync synapseClient, GWTWrapper gwt) {
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
	public void getEntityHeader(String entityId, AsyncCallback<EntityHeader> callback) {
		List<AsyncCallback<EntityHeader>> list = reference2Callback.get(entityId);
		if (list == null) {
			list = new ArrayList<AsyncCallback<EntityHeader>>();
			reference2Callback.put(entityId, list);
		}
		list.add(callback);
	}
	
	public void executeRequests() {
		if (!reference2Callback.isEmpty()) {
			final Map<String, List<AsyncCallback<EntityHeader>>> reference2CallbackCopy = new HashMap<String, List<AsyncCallback<EntityHeader>>>();
			reference2CallbackCopy.putAll(reference2Callback);
			reference2Callback.clear();
			List<String> entityIdsList = new ArrayList<String>();
			entityIdsList.addAll(reference2CallbackCopy.keySet());
			synapseClient.getEntityHeaderBatch(entityIdsList,new AsyncCallback<ArrayList<EntityHeader>>() {
				@Override
				public void onFailure(Throwable caught) {
					// go through all requested objects, and inform them of the error
					for (String entityId: reference2CallbackCopy.keySet()) {
						callOnFailure(entityId, caught);
					}
				}
				
				private void callOnFailure(String entityId, Throwable ex) {
					List<AsyncCallback<EntityHeader>> callbacks = reference2CallbackCopy.get(entityId);
					if (callbacks != null) {
						for (AsyncCallback<EntityHeader> callback : callbacks) {
							callback.onFailure(ex);	
						}
					}
				}
				
				public void onSuccess(ArrayList<EntityHeader> results) {
					// go through all results, and inform the proper callback of the success
					for (EntityHeader entityHeader : results) {
						List<AsyncCallback<EntityHeader>> callbacks = reference2CallbackCopy.remove(entityHeader.getId());
						if (callbacks != null) {
							for (AsyncCallback<EntityHeader> callback : callbacks) {
								callback.onSuccess(entityHeader);	
							}
						}
					}
					UnknownErrorException notReturnedException = new UnknownErrorException(DisplayConstants.ERROR_LOADING);
					for (String entityId : reference2CallbackCopy.keySet()) {
						// not returned
						callOnFailure(entityId, notReturnedException);
						
					}
				};
			});
		}
	}
	
	
}
