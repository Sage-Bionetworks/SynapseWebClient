package org.sagebionetworks.web.client.widget.asynch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class BaseEntityHeaderAsyncHandlerImpl {
	private Map<Reference, List<AsyncCallback<EntityHeader>>> reference2Callback = new HashMap<Reference, List<AsyncCallback<EntityHeader>>>();
	SynapseJavascriptClient jsClient;
	boolean isUsingVersion;
	public BaseEntityHeaderAsyncHandlerImpl(SynapseJavascriptClient jsClient, GWTWrapper gwt) {
		this.jsClient = jsClient;
		Callback callback = new Callback() {
			@Override
			public void invoke() {
				executeRequests();
			}
		};
		gwt.scheduleFixedDelay(callback, 200 + gwt.nextInt(150));
	}

	protected void getEntityHeaderShared(String entityId, Long versionNumber, AsyncCallback<EntityHeader> callback) {
		Reference ref = new Reference();
		ref.setTargetId(entityId);
		ref.setTargetVersionNumber(versionNumber);
		
		List<AsyncCallback<EntityHeader>> list = reference2Callback.get(ref);
		if (list == null) {
			list = new ArrayList<AsyncCallback<EntityHeader>>();
			reference2Callback.put(ref, list);
		}
		list.add(callback);
	}

	public void executeRequests() {
		if (!reference2Callback.isEmpty()) {
			final Map<Reference, List<AsyncCallback<EntityHeader>>> reference2CallbackCopy = new HashMap<Reference, List<AsyncCallback<EntityHeader>>>();
			reference2CallbackCopy.putAll(reference2Callback);
			reference2Callback.clear();
			List<Reference> entityIdsList = new ArrayList<Reference>();
			entityIdsList.addAll(reference2CallbackCopy.keySet());
			jsClient.getEntityHeaderBatchFromReferences(entityIdsList, new AsyncCallback<ArrayList<EntityHeader>>() {
				@Override
				public void onFailure(Throwable caught) {
					// go through all requested objects, and inform them of the error
					for (Reference entityId : reference2CallbackCopy.keySet()) {
						callOnFailure(entityId, caught);
					}
				}

				private void callOnFailure(Reference entityId, Throwable ex) {
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
						Reference ref = new Reference();
						ref.setTargetId(entityHeader.getId());
						if (isUsingVersion) {
							ref.setTargetVersionNumber(entityHeader.getVersionNumber());	
						} else {
							ref.setTargetVersionNumber(null);
						}
						List<AsyncCallback<EntityHeader>> callbacks = reference2CallbackCopy.remove(ref);
						if (callbacks != null) {
							for (AsyncCallback<EntityHeader> callback : callbacks) {
								callback.onSuccess(entityHeader);
							}
						}
					}
					NotFoundException notReturnedException = new NotFoundException(DisplayConstants.ERROR_LOADING);
					for (Reference entityId : reference2CallbackCopy.keySet()) {
						// not returned
						callOnFailure(entityId, notReturnedException);

					}
				};
			});
		}
	}


}
