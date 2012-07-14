package org.sagebionetworks.web.client.widget.licenseddownloader;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("license")
public interface LicenceService extends RemoteService {
	
	/**
	 * Logs the uer's download 
	 * @param username
	 * @param objectUri 
	 */
	public void logUserDownload(String username, String objectUri, String fileUri);
	
}
