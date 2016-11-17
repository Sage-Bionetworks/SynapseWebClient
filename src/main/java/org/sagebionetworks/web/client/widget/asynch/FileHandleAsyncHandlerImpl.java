package org.sagebionetworks.web.client.widget.asynch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.file.BatchFileRequest;
import org.sagebionetworks.repo.model.file.BatchFileResult;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class FileHandleAsyncHandlerImpl implements FileHandleAsyncHandler {
	private Map<String, List<AsyncCallback<FileResult>>> reference2Callback = new HashMap<String, List<AsyncCallback<FileResult>>>();
	private List<FileHandleAssociation> fileHandleAssociations = new ArrayList<FileHandleAssociation>();
	SynapseClientAsync synapseClient;
	// This singleton checks for new work every <DELAY> milliseconds.
	public static final int DELAY = 325;
	public static final int LIMIT = 95;
	
	@Inject
	public FileHandleAsyncHandlerImpl(SynapseClientAsync synapseClient, GWTWrapper gwt) {
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
	public void getFileHandle(FileHandleAssociation fileHandleAssociation, AsyncCallback<FileResult> callback) {
		List<AsyncCallback<FileResult>> list = reference2Callback.get(fileHandleAssociation.getFileHandleId());
		if (list == null) {
			list = new ArrayList<AsyncCallback<FileResult>>();
			reference2Callback.put(fileHandleAssociation.getFileHandleId(), list);
			fileHandleAssociations.add(fileHandleAssociation);
		}
		list.add(callback);

		// if we are getting close to the limit, then force execute the batch
		if (reference2Callback.size() > LIMIT) {
			executeRequests();
		}
	}
	
	public void executeRequests() {
		if (!reference2Callback.isEmpty()) {
			final Map<String, List<AsyncCallback<FileResult>>> reference2CallbackCopy = new HashMap<String, List<AsyncCallback<FileResult>>>();
			reference2CallbackCopy.putAll(reference2Callback);
			List<FileHandleAssociation> fileHandleAssociationsCopy = new ArrayList<FileHandleAssociation>();
			fileHandleAssociationsCopy.addAll(fileHandleAssociations);
			reference2Callback.clear();
			fileHandleAssociations.clear();
			BatchFileRequest request = new BatchFileRequest();
			request.setRequestedFiles(fileHandleAssociationsCopy);
			request.setIncludeFileHandles(true);
			request.setIncludePreSignedURLs(false);
			synapseClient.getFileHandleAndUrlBatch(request,new AsyncCallback<BatchFileResult>() {
				@Override
				public void onFailure(Throwable caught) {
					// go through all requested objects, and inform them of the error
					for (String fileHandleId: reference2CallbackCopy.keySet()) {
						callOnFailure(fileHandleId, caught);
					}
				}
				
				private void callOnFailure(String fileHandleId, Throwable ex) {
					List<AsyncCallback<FileResult>> callbacks = reference2CallbackCopy.get(fileHandleId);
					if (callbacks != null) {
						for (AsyncCallback<FileResult> callback : callbacks) {
							callback.onFailure(ex);	
						}
					}
				}
				
				public void onSuccess(BatchFileResult results) {
					// go through all results, and inform the proper callback of the success
					for (FileResult fileResult : results.getRequestedFiles()) {
						List<AsyncCallback<FileResult>> callbacks = reference2CallbackCopy.remove(fileResult.getFileHandleId());
						if (callbacks != null) {
							for (AsyncCallback<FileResult> callback : callbacks) {
								callback.onSuccess(fileResult);	
							}
						}
					}
					UnknownErrorException notReturnedException = new UnknownErrorException(DisplayConstants.ERROR_LOADING);
					for (String fileHandleId : reference2CallbackCopy.keySet()) {
						// not returned
						callOnFailure(fileHandleId, notReturnedException);
						
					}
				};
			});
		}
	}
	
	
}
