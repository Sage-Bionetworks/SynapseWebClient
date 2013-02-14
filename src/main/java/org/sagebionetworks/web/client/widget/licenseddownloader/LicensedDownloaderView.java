package org.sagebionetworks.web.client.widget.licenseddownloader;

import java.util.List;

import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.utils.APPROVAL_TYPE;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;

import com.google.gwt.user.client.ui.IsWidget;

public interface LicensedDownloaderView extends IsWidget, SynapseWidgetView {
	
	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	/**
	 *
	 */
	public void setRestrictionLevel(RESTRICTION_LEVEL restrictionLevel);
	
	/**
	 * set the approval type (USER_AGREEMENT or ACT_APPROVAL) or NONE if access is allowed with no add'l approval
	 * @param approvalType
	 */
	public void setApprovalType(APPROVAL_TYPE approvalType);
	
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
