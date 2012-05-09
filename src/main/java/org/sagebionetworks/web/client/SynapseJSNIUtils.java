package org.sagebionetworks.web.client;

public interface SynapseJSNIUtils {

	public void recordPageVisit(String token);
	
	public String getCurrentHistoryToken();
}
