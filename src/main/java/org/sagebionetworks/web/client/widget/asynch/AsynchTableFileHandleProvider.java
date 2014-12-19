package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.shared.table.CellAddress;

import com.google.gwt.core.client.Callback;
/**
 * Abstraction for a Asynchronous service that aggregates file handle request for table.s
 *  
 * @author John
 *
 */
public interface AsynchTableFileHandleProvider {
	
	/**
	 * Request a FileHandle for an address.
	 * @param address
	 * @param callback
	 */
	public void requestFileHandle(CellAddress address, Callback<FileHandle, Throwable> callback);

}
