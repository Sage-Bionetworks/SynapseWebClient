package org.sagebionetworks.web.client.widget.licenseddownloader;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;

public interface LicensedDownloaderView extends SynapseView {
	
	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void newWindow(String url);
	
	/**
	 * Presenter Interface 
	 */
	public interface Presenter {
		
		public boolean isDownloadAllowed();

		void setEntityUpdatedHandler(EntityUpdatedHandler handler);
	}
}
