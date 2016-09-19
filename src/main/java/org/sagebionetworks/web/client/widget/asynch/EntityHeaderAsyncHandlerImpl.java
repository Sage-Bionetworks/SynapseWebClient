package org.sagebionetworks.web.client.widget.asynch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class EntityHeaderAsyncHandlerImpl implements EntityHeaderAsyncHandler {
	private Map<Reference, AsyncCallback<EntityHeader>> reference2Callback = new HashMap<Reference, AsyncCallback<EntityHeader>>();
	private List<Reference> referenceArrayList = new ArrayList<Reference>();
	SynapseClientAsync synapseClient;
	@Inject
	public EntityHeaderAsyncHandlerImpl(SynapseClientAsync synapseClient, GWTWrapper gwt) {
		this.synapseClient = synapseClient;
		Callback callback = new Callback() {
			@Override
			public void invoke() {
				executeRequests();
			}
		};
		gwt.scheduleFixedDelay(callback, 300);
	}
	
	@Override
	public void getEntityHeader(Reference entityReference, AsyncCallback<EntityHeader> callback) {
		referenceArrayList.add(entityReference);
		reference2Callback.put(entityReference, callback);
	}
	
	public void executeRequests() {
		if (!referenceArrayList.isEmpty()) {
			ReferenceList referenceList = new ReferenceList();
			final List<Reference> referenceListCopy = new ArrayList<Reference>();
			referenceListCopy.addAll(referenceArrayList);
			referenceList.setReferences(referenceListCopy);
			referenceArrayList.clear();
			synapseClient.getEntityHeaderBatch(referenceList,new AsyncCallback<PaginatedResults<EntityHeader>>() {
				@Override
				public void onFailure(Throwable caught) {
					// go through all requested objects, and inform them of the error
					for (Reference reference : referenceListCopy) {
						AsyncCallback<EntityHeader> callback = reference2Callback.remove(reference);
						if (callback != null) {
							callback.onFailure(caught);
						}
					}
					reference2Callback.clear();
				}
				public void onSuccess(PaginatedResults<EntityHeader> result) {
					// go through all results, and inform the proper callback of the success
					List<EntityHeader> results = result.getResults();
					// sanity check
					if (results.size() != referenceListCopy.size()) {
						onFailure(new UnknownErrorException(DisplayConstants.ERROR_LOADING));
					} else {
						for (int i = 0; i < results.size(); i++) {
							AsyncCallback<EntityHeader> callback = reference2Callback.remove(referenceListCopy.get(i));
							if (callback != null) {
								callback.onSuccess(results.get(i));
							}
						}
					}
					reference2Callback.clear();
				};
			});
		}
	}
	
	
}
