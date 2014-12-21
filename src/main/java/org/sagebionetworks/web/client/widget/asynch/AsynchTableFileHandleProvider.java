package org.sagebionetworks.web.client.widget.asynch;

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
	public void requestFileHandle(TableFileHandleRequest request);

}
