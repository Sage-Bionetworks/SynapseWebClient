package org.sagebionetworks.web.client.widget.table.modal.download;

import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;

/**
 * This page tell the user their file is ready for download. When the user selects the download
 * button, a pre-signed URL will be generated and sent to the browser.
 * 
 * @author jhill
 *
 */
public interface DownloadFilePage extends ModalPage {

	/**
	 * Configure this widget before using.
	 * 
	 * @param resultsFileHandleId The fileHandle to be downloaded.
	 */
	void configure(String resultsFileHandleId);

}
