package org.sagebionetworks.web.client;


public interface GlobalApplicationStateView {
	void showVersionOutOfDateGlobalMessage();

	void initGlobalViewProperties();

	void showGetVersionError(String error);

	void back();

	void initSRCEndpoints(String repoEndpoint, String portalEndpoint);
}
