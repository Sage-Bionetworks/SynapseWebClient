package org.sagebionetworks.web.client.widget.asynch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sagebionetworks.repo.model.file.BatchFileRequest;
import org.sagebionetworks.repo.model.file.BatchFileResult;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class BaseFileHandleAsyncHandlerImpl {

	abstract protected boolean isIncludeFileHandles();

	abstract protected boolean isIncludePreSignedURLs();

	private Map<String, List<AsyncCallback<FileResult>>> reference2Callback = new HashMap<String, List<AsyncCallback<FileResult>>>();
	private List<FileHandleAssociation> fileHandleAssociations = new ArrayList<FileHandleAssociation>();
	SynapseJavascriptClient jsClient;
	public static final int LIMIT = 95;

	private ClientCache clientCache;
	private AdapterFactory adapterFactory;
	private GWTWrapper gwt;

	public BaseFileHandleAsyncHandlerImpl(SynapseJavascriptClient jsClient, GWTWrapper gwt, ClientCache clientCache, AdapterFactory adapterFactory) {
		this.jsClient = jsClient;
		this.clientCache = clientCache;
		this.adapterFactory = adapterFactory;
		this.gwt = gwt;
		Callback callback = new Callback() {
			@Override
			public void invoke() {
				executeRequests();
			}
		};
		gwt.scheduleFixedDelay(callback, 200 + gwt.nextInt(150));
	}

	public void getFileResult(FileHandleAssociation fileHandleAssociation, AsyncCallback<FileResult> callback) {
		if (isIncludeFileHandles() && !isIncludePreSignedURLs() && clientCache.contains(fileHandleAssociation.getFileHandleId() + WebConstants.FILE_HANDLE_SUFFIX)) {
			FileResult fileResult = getFileResultFromCache(fileHandleAssociation.getFileHandleId(), adapterFactory, clientCache);
			if (fileResult != null) {
				// done!
				gwt.scheduleDeferred(() -> {
					callback.onSuccess(fileResult);
				});
				return;
			}
		}

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
			request.setIncludeFileHandles(isIncludeFileHandles());
			request.setIncludePreSignedURLs(isIncludePreSignedURLs());
			jsClient.getFileHandleAndUrlBatch(request, new AsyncCallback<BatchFileResult>() {
				@Override
				public void onFailure(Throwable caught) {
					// go through all requested objects, and inform them of the error
					for (String fileHandleId : reference2CallbackCopy.keySet()) {
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
							if (fileResult.getFailureCode() != null) {
								Exception ex = null;
								switch (fileResult.getFailureCode()) {
									case NOT_FOUND:
										ex = new NotFoundException();
										break;
									case UNAUTHORIZED:
										ex = new ForbiddenException();
										break;
								}
								for (AsyncCallback<FileResult> callback : callbacks) {
									callback.onFailure(ex);
								}
							} else {
								if (isIncludeFileHandles() && !isIncludePreSignedURLs()) {
									cacheFileResult(fileResult);
								}
								for (AsyncCallback<FileResult> callback : callbacks) {
									callback.onSuccess(fileResult);
								}
							}
						}
					}
					NotFoundException notReturnedException = new NotFoundException(DisplayConstants.ERROR_LOADING);
					for (String fileHandleId : reference2CallbackCopy.keySet()) {
						// not returned
						callOnFailure(fileHandleId, notReturnedException);

					}
				};
			});
		}
	}

	public static FileResult getFileResultFromCache(String fileHandleId, AdapterFactory adapterFactory, ClientCache clientCache) {
		String fileResultString = clientCache.get(fileHandleId + WebConstants.FILE_HANDLE_SUFFIX);
		if (fileResultString != null) {
			try {
				return new FileResult(adapterFactory.createNew(fileResultString));
			} catch (JSONObjectAdapterException e) {
				// if any problems occur, try to get the file handle with a rpc
			}
		}
		return null;
	}

	public void cacheFileResult(FileResult result) {
		JSONObjectAdapter adapter = adapterFactory.createNew();
		try {
			result.writeToJSONObject(adapter);
			clientCache.put(result.getFileHandleId() + WebConstants.FILE_HANDLE_SUFFIX, adapter.toJSONString());
		} catch (JSONObjectAdapterException e) {
		}
	}

}
