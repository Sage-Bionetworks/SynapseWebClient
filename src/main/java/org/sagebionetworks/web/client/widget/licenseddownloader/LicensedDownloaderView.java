package org.sagebionetworks.web.client.widget.licenseddownloader;

import java.util.List;

import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.utils.APPROVAL_REQUIRED;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;
import org.sagebionetworks.web.shared.FileDownload;

import com.google.gwt.user.client.ui.IsWidget;

public interface LicensedDownloaderView extends IsWidget, SynapseWidgetView {
	
	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	/**
	 * Make the view show the License acceptance view first
	 * @param licenseRequired
	 */
	public void setApprovalRequired(APPROVAL_REQUIRED approvalRequired);
	
	/**
	 * Set the license text to display
	 * @param licenseHtml
	 */
	public void setLicenseHtml(String licenseHtml);	
	
	/**
	 * Show the License Box window
	 */
	public void showWindow();
	
	/**
	 * Shows loading in the contents window
	 */
	public void showDownloadsLoading();

	
	/**
	 * Sets the content of the download pane
	 * @param downloads
	 */
	public void setDownloadUrls(List<FileDownload> downloads);

	/**
	 * Sets the content of the download pane
	 * @param downloads
	 */
	public void setDownloadLocations(List<LocationData> locations, String md5);
	
	
	/**
	 * Presenter Interface 
	 */
	public interface Presenter {
		
		/**
		 * Call when the user accepts the presented License Agreement
		 */
		public void setLicenseAccepted();
		
		public boolean isDownloadAllowed();

		public Callback getTermsOfUseCallback();

		public Callback getRequestAccessCallback();

		void clearHandlers();

		void addEntityUpdatedHandler(EntityUpdatedHandler handler);

	}

	public void showDownloadFailure();
	
	public void setNoDownloads();

	public void setUnauthorizedDownloads();

	public void setNeedToLogIn();
	
}
