package org.sagebionetworks.web.client.widget.asynch;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.table.RowReference;
import org.sagebionetworks.repo.model.table.RowReferenceSet;
import org.sagebionetworks.repo.model.table.SelectColumn;

import com.google.gwt.core.client.Callback;

/**
 * Batch of of table FileHandle requests.
 * @author John
 *
 */
public class BatchTableFileHandleRequests {
	
	private String tableId;
	private List<TableFileHandleRequest> requests = new LinkedList<TableFileHandleRequest>();
	
	/**
	 * Create a new batch for a single table.
	 * @param tableId
	 */
	public BatchTableFileHandleRequests(String tableId) {
		super();
		this.tableId = tableId;
	}

	/**
	 * Add a request to this batch.
	 * @param request
	 */
	public void addRequest(TableFileHandleRequest request){
		this.requests.add(request);
	}
	
	/**
	 * Build a RowReferenceSet for this batch.
	 * @return
	 */
	public RowReferenceSet buildReferenceSet(){
		RowReferenceSet set = new RowReferenceSet();
		set.setTableId(this.tableId);
		set.setRows(new LinkedList<RowReference>());
		// Key track of the rows keys so we only request each row once.
		Set<String> rowKeys = new HashSet<String>();
		// Keep the columnIds in the order we find them.
		Set<String> columnIds = new LinkedHashSet<String>();
		for(TableFileHandleRequest request: requests){
			// row key = 'rowId-rowVersionNumber'
			String rowKey = request.getAddress().getRowKey();
			if(!rowKeys.contains(rowKey)){
				// first time this row has been hit so add a RowReference
				RowReference ref = new RowReference();
				ref.setRowId(request.getAddress().getRowId());
				ref.setVersionNumber(request.getAddress().getRowVersion());
				set.getRows().add(ref);
				rowKeys.add(rowKey);
			}
			// Add each columnId to the set.
			columnIds.add(request.getAddress().getColumnId());
		}
		// Build the header from the unique columnIds.
		List<SelectColumn> header = new LinkedList<SelectColumn>();
		for(String columnId: columnIds){
			SelectColumn sc = new SelectColumn();
			sc.setId(columnId);
			header.add(sc);
		}
		set.setHeaders(header);
		return set;
	}
	
	/**
	 * Build a map of FileHandle.id to a list of callbacks.  Multiple callbacks might need to be called for each fileHandle.
	 * @return
	 */
	public Map<String, List<Callback<FileHandle, Throwable>>> buildMapOfFileHandleIdToCallback(){
		Map<String, List<Callback<FileHandle, Throwable>>> map = new LinkedHashMap<String, List<Callback<FileHandle,Throwable>>>();
		for(TableFileHandleRequest request: requests){
			List<Callback<FileHandle, Throwable>> list = map.get(request.getFileHandleId());
			if(list == null){
				list = new LinkedList<Callback<FileHandle,Throwable>>();
				map.put(request.getFileHandleId(), list);
			}
			list.add(request.getCallback());
		}
		return map;
	}
	
	/**
	 * Get all callbacks for this batch.
	 * @return
	 */
	public List<Callback<FileHandle, Throwable>> getAllCallbacks(){
		List<Callback<FileHandle, Throwable>> list = new LinkedList<Callback<FileHandle,Throwable>>();
		for(TableFileHandleRequest request: requests){
			list.add(request.getCallback());
		}
		return list;
	}
}
