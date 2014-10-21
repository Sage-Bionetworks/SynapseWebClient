package org.sagebionetworks.web.client.widget.table.modal.download;

import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;

public interface DownloadFilePage extends ModalPage {

	/**
	 * Configure this widget before using.
	 * 
	 * @param resultsFileHandleId The fileHandle to be downloaded.
	 */
	void configure(String resultsFileHandleId);

}
