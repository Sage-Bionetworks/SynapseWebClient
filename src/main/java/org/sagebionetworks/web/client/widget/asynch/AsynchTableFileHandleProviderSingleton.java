package org.sagebionetworks.web.client.widget.asynch;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.repo.model.table.RowReference;
import org.sagebionetworks.repo.model.table.RowReferenceSet;
import org.sagebionetworks.repo.model.table.SelectColumn;
import org.sagebionetworks.repo.model.table.TableFileHandleResults;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.asynch.TimerProvider.FireHandler;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.table.CellAddress;

import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * This implementation is designed to be a Singleton that gather request from many source,
 * run the request on a timer and and then broadcast the results when back to each source.
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
	List<CellRequest> requestQueue;
	/**
	 * All 
	 */
	Map<CellAddress, Callback<FileHandle, RestServiceException>> awaitingResponse;
	
	
	@Inject
	public AsynchTableFileHandleProviderSingleton(
			SynapseClientAsync synpaseClient, TimerProvider timerProvider) {
		super();
		this.synpaseClient = synpaseClient;
		this.timerProvider = timerProvider;
		this.requestQueue = new LinkedList<CellRequest>();
		timerProvider.setHandler(this);
	}


	@Override
	public void requestFileHandle(CellAddress address,
			Callback<FileHandle, Throwable> callback) {
		// Add this request to the queue.
		requestQueue.add(new CellRequest(address, callback));
		// Start the timer if it is not running.
		if(!timerRunning){
			// Continue gathering request for one second.
			timerProvider.schedule(250);
			timerRunning = true;
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
		Map<String, TableRequest> requestMap = new LinkedHashMap<String, TableRequest>();
		Iterator<CellRequest> it = requestQueue.iterator();
		while(it.hasNext()){
			CellRequest request = it.next();
			// Originally we were going to batch by table but the server was
			// not keeping the batches strait so now we just batch by row.
			String rowKey = request.address.getTableId()+"-"+request.address.getRowId();
			TableRequest table = requestMap.get(rowKey);
			if(table == null){
				table = new TableRequest(request.address.getTableId());
				requestMap.put(rowKey, table);
			}
			// Add this request to its table.
			table.add(request);
			// remove this from the queue.
			it.remove();
		}
		
		// Now make a service call for each request
		for(TableRequest tableRequest: requestMap.values()){
			final TableRequest thisRequest = tableRequest;
			// Make the service call
			synpaseClient.getTableFileHandle(tableRequest.buildReferenceSet(), new AsyncCallback<TableFileHandleResults>() {
				
				@Override
				public void onSuccess(TableFileHandleResults result) {
					requestSuccess(thisRequest, result);
				}

				@Override
				public void onFailure(Throwable caught) {
					// Forward the failures to each handler
					requestFailed(thisRequest, caught);
				}
			});
		}
	}
	/**
	 * Called after results are successfully fetched.
	 * The order of the rows in both the request and results must match.
	 * @param thisRequest
	 * @param result
	 */
	private void requestSuccess(TableRequest thisRequest,TableFileHandleResults result) {
		// Walk the responses
		for(int row=0; row<result.getRows().size(); row++){
			FileHandleResults handles = result.getRows().get(row);
			RowRequest rowRequst = thisRequest.get(row);
			for(int col=0; col<result.getHeaders().size(); col++){
				FileHandle handle = handles.getList().get(col);
				String columnId = result.getHeaders().get(col).getId();
				for(CellRequest cell: rowRequst.getCellsWithColumnId(columnId)){
					try {
						cell.callback.onSuccess(handle);
					} catch (Throwable e) {
						// Errors in one cell should not propagate to other cells
						cell.callback.onFailure(e);
					}
				}
			}
		}
	}
	
	/**
	 * Forward the failure to all callers.
	 * @param thisRequest
	 * @param caught
	 */
	private void requestFailed(TableRequest thisRequest, Throwable caught) {
		// Pass along the error
		for(CellRequest cellRequst: thisRequest.getAllCells()){
			cellRequst.callback.onFailure(caught);
		}
	}

	/**
	 * Helps track all request for a table.
	 *
	 */
	private static class TableRequest {
		String tableId;
		List<RowRequest> rows = new LinkedList<RowRequest>();
		Set<String> columnIds = new LinkedHashSet<String>();
		public TableRequest(String tableId) {
			this.tableId = tableId;
		}

		/**
		 * Add a cell to this table.
		 * @param toAdd
		 */
		void add(CellRequest toAdd){
			columnIds.add(toAdd.address.getColumnId());
			for(RowRequest row: rows){
				if(row.rowId.equals(toAdd.address.getRowId()) && row.rowVersion.equals(toAdd.address.getRowVersion())){
					row.cellRequests.add(toAdd);
					return;
				}
			}
			RowRequest newRow = new RowRequest(toAdd.address.getRowId(), toAdd.address.getRowVersion());
			rows.add(newRow);
			newRow.cellRequests.add(toAdd);
		}
		
		/**
		 * Build a RowReferenceSet for this table.
		 * @return
		 */
		RowReferenceSet buildReferenceSet(){
			RowReferenceSet set = new RowReferenceSet();
			set.setTableId(this.tableId);
			set.setRows(new LinkedList<RowReference>());
			for(RowRequest row: rows){
				RowReference ref = new RowReference();
				ref.setRowId(row.rowId);
				ref.setVersionNumber(row.rowVersion);
				set.getRows().add(ref);
			}
			List<SelectColumn> header = new LinkedList<SelectColumn>();
			for(String columnId: columnIds){
				SelectColumn sc = new SelectColumn();
				sc.setId(columnId);
				header.add(sc);
			}
			set.setHeaders(header);
			return set;
		}
		
		Iterable<CellRequest> getAllCells(){
			List<CellRequest> list = new LinkedList<CellRequest>();
			for(RowRequest row: rows){
				list.addAll(row.cellRequests);
			}
			return list;
		}
		
		public RowRequest get(int index){
			return rows.get(index);
		}
	}
	/**
	 * Tracks a single row and all request associated with it.
	 * @author John
	 *
	 */
	private static class RowRequest {
		Long rowId;
		Long rowVersion;
		List<CellRequest> cellRequests = new LinkedList<CellRequest>();
		RowRequest(Long rowId, Long rowVersion){
			this.rowId = rowId;
			this.rowVersion = rowVersion;
		}
		
		/**
		 * Get all cells that have the given column ID.
		 * @param columnId
		 * @return
		 */
		Iterable<CellRequest> getCellsWithColumnId(String columnId){
			List<CellRequest> list = new LinkedList<CellRequest>();
			// Add each cell that matches the columnId
			for(CellRequest request: cellRequests){
				if(request.address.getColumnId().equals(columnId)){
					list.add(request);
				}
			}
			return list;
		}
	}
	
	private static class CellRequest {
		CellAddress address;
		Callback<FileHandle, Throwable> callback;
		public CellRequest(CellAddress address,
				Callback<FileHandle, Throwable> callback) {
			super();
			this.address = address;
			this.callback = callback;
		}
	}
}
