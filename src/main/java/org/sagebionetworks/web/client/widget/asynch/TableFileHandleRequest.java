package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.shared.table.CellAddress;
import com.google.gwt.core.client.Callback;

/**
 * Object used to request a FileHandle from a table. This object is immutable.
 * 
 * @author John
 *
 */
public class TableFileHandleRequest {
	String fileHandleId;
	CellAddress address;
	Callback<FileHandle, Throwable> callback;

	/**
	 * 
	 * @param fileHandleId The ID of the requested file handle.
	 * @param address The full cell address.
	 * @param callback
	 */
	public TableFileHandleRequest(String fileHandleId, CellAddress address, Callback<FileHandle, Throwable> callback) {
		super();
		this.fileHandleId = fileHandleId;
		this.address = address;
		this.callback = callback;
	}

	/**
	 * The ID of the requested file handle.
	 * 
	 * @return
	 */
	public String getFileHandleId() {
		return fileHandleId;
	}

	/**
	 * The full cell address.
	 * 
	 * @return
	 */
	public CellAddress getAddress() {
		return address;
	}

	/**
	 * The handler to be called after the with the results.
	 * 
	 * @return
	 */
	public Callback<FileHandle, Throwable> getCallback() {
		return callback;
	}

}
