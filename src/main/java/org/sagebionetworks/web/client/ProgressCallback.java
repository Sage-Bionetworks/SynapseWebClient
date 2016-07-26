package org.sagebionetworks.web.client;

public interface ProgressCallback {
	void updateProgress(double loaded, double total);
}
