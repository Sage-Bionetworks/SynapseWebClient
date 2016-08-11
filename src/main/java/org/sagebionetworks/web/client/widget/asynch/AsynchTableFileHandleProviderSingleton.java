package org.sagebionetworks.web.client.widget.asynch;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.repo.model.table.TableFileHandleResults;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.asynch.TimerProvider.FireHandler;

import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * This implementation is designed to be a Singleton. It will queue up request from many sources
 * and start a timer.  When the timer fires all requests on the queue are batched into a single request per table.
 * The results from a batch request are then broadcast back to each provided callback based on the requested fileHandl.
 * @author John
 *
 */
public class AsynchTableFileHandleProviderSingleton implements AsynchTableFileHandleProvider, FireHandler {
	
	SynapseClientAsync synpaseClient;
	TimerProvider timerProvider;
	boolean timerRunning = false;
	
	/**
	 * The queue of request
	 */
	List<TableFileHandleRequest> requestQueue;
	
	
	@Inject
	public AsynchTableFileHandleProviderSingleton(
			SynapseClientAsync synpaseClient, TimerProvider timerProvider) {
		super();
		this.synpaseClient = synpaseClient;
		this.timerProvider = timerProvider;
		this.requestQueue = new LinkedList<TableFileHandleRequest>();
		timerProvider.setHandler(this);
	}


	@Override
	public void requestFileHandle(TableFileHandleRequest request) {
		// Add this request to the queue.
		requestQueue.add(request);
		// Start the timer if it is not running.
		if(!timerRunning){
			// Continue gathering request for one second.
			timerRunning = true;
			timerProvider.schedule(250);
		}
	}


	/**
	 * Called when the timer fires.
	 */
	@Override
	public void fire() {
		// the timer is now longer running
		timerRunning = false;
		// First build up a request for each table.
		Map<String, BatchTableFileHandleRequests> requestMap = new LinkedHashMap<String, BatchTableFileHandleRequests>();
		Iterator<TableFileHandleRequest> it = requestQueue.iterator();
		while(it.hasNext()){
			TableFileHandleRequest request = it.next();
			// Batch requests by table.
			String tableId = request.address.getTableId();
			BatchTableFileHandleRequests table = requestMap.get(tableId);
			if(table == null){
				table = new BatchTableFileHandleRequests(request.address.getTableId());
				requestMap.put(tableId, table);
			}
			// Add this request to its table.
			table.addRequest(request);
			// remove this from the queue.
			it.remove();
		}
		
		// Now make a service call for each request
		for(BatchTableFileHandleRequests tableRequest: requestMap.values()){
			final BatchTableFileHandleRequests thisRequest = tableRequest;
			// Make the service call
			synpaseClient.getTableFileHandle(tableRequest.buildReferenceSet(), new AsyncCallback<TableFileHandleResults>() {
				
				@Override
				public void onSuccess(TableFileHandleResults result) {
					requestSuccess(thisRequest.buildMapOfFileHandleIdToCallback(), result);
				}

				@Override
				public void onFailure(Throwable caught) {
					// Forward the failures to each handler
					requestFailed(thisRequest.getAllCallbacks(), caught);
				}
			});
		}
	}
	/**
	 * Called after results are successfully fetched.
	 * The order of the rows in both the request and results must match.
	 * @param thisRequest
	 * @param valid
	 */
	private void requestSuccess(Map<String, List<Callback<FileHandle, Throwable>>> map, TableFileHandleResults results) {
		// Extract all unique fileHandles.
		Map<String, FileHandle> fileHandlesMap = new LinkedHashMap<String, FileHandle>();
		if(results.getRows() != null){
			for(FileHandleResults list: results.getRows()){
				if(list != null){
					for(FileHandle handle: list.getList()){
						if(handle != null){
							fileHandlesMap.put(handle.getId(), handle);
						}
					}
				}
			}
		}

		// Notify all callbacks for each file handle
		for(FileHandle handle: fileHandlesMap.values()){
			List<Callback<FileHandle, Throwable>> list = map.get(handle.getId());
			for(Callback<FileHandle, Throwable> callback: list){
				try {
					callback.onSuccess(handle);
				} catch (Throwable e) {
					// Errors in one cell should not propagate to other cells
					callback.onFailure(e);
				}
			}
		}
	}
	
	/**
	 * Forward the failure to all callers.
	 * @param thisRequest
	 * @param caught
	 */
	private void requestFailed(List<Callback<FileHandle, Throwable>> allCallbacks, Throwable caught) {
		// Pass along the error
		for(Callback<FileHandle, Throwable> callback: allCallbacks){
			callback.onFailure(caught);
		}
	}

}
