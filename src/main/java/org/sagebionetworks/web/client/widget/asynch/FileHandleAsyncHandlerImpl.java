package org.sagebionetworks.web.client.widget.asynch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.EntityHeader;
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
	private Map<FileHandleAssociation, List<AsyncCallback<FileResult>>> reference2Callback = new HashMap<FileHandleAssociation, List<AsyncCallback<FileResult>>>();
	SynapseClientAsync synapseClient;
	// This singleton checks for new work every <DELAY> milliseconds.
	public static final int DELAY = 300;
	
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
		List<AsyncCallback<FileResult>> list = reference2Callback.get(fileHandleAssociation);
		if (list == null) {
			list = new ArrayList<AsyncCallback<FileResult>>();
			reference2Callback.put(fileHandleAssociation, list);
		}
		list.add(callback);
	}
	
	public void executeRequests() {
		if (!reference2Callback.isEmpty()) {
			final Map<FileHandleAssociation, List<AsyncCallback<FileResult>>> reference2CallbackCopy = new HashMap<FileHandleAssociation, List<AsyncCallback<FileResult>>>();
			reference2CallbackCopy.putAll(reference2Callback);
			reference2Callback.clear();
			List<FileHandleAssociation> fileHandleAssociationsList = new ArrayList<FileHandleAssociation>();
			fileHandleAssociationsList.addAll(reference2CallbackCopy.keySet());
			BatchFileRequest request = new BatchFileRequest();
			request.setRequestedFiles(fileHandleAssociationsList);
			request.setIncludeFileHandles(true);
			synapseClient.getFileHandleAndUrlBatch(request,new AsyncCallback<BatchFileResult>() {
				@Override
				public void onFailure(Throwable caught) {
					// go through all requested objects, and inform them of the error
					for (FileHandleAssociation fileHandleAssociation: reference2CallbackCopy.keySet()) {
						callOnFailure(fileHandleAssociation, caught);
					}
				}
				
				private void callOnFailure(FileHandleAssociation fileHandleAssociation, Throwable ex) {
					List<AsyncCallback<FileResult>> callbacks = reference2CallbackCopy.get(fileHandleAssociation);
					if (callbacks != null) {
						for (AsyncCallback<FileResult> callback : callbacks) {
							callback.onFailure(ex);	
						}
					}
				}
				
				public void onSuccess(BatchFileResult results) {
					// go through all results, and inform the proper callback of the success
					for (FileResult entityHeader : results.getRequestedFiles()) {
						List<AsyncCallback<FileResult>> callbacks = reference2CallbackCopy.remove(entityHeader);
						if (callbacks != null) {
							for (AsyncCallback<FileResult> callback : callbacks) {
								callback.onSuccess(entityHeader);	
							}
						}
					}
					UnknownErrorException notReturnedException = new UnknownErrorException(DisplayConstants.ERROR_LOADING);
					for (FileHandleAssociation fileHandleAssociation : reference2CallbackCopy.keySet()) {
						// not returned
						callOnFailure(fileHandleAssociation, notReturnedException);
						
					}
				};
			});
		}
	}
	
	
}
