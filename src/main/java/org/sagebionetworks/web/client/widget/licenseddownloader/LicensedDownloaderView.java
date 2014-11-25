package org.sagebionetworks.web.client.widget.licenseddownloader;

import java.util.List;

import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.utils.APPROVAL_TYPE;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface LicensedDownloaderView extends IsWidget, SynapseView {
	
	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
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
	@Deprecated
	public void setDownloadLocations(List<LocationData> locations, String md5);
	
	public void setDownloadLocation(String md5, String directDownloadUrl);
	
	/**
	 * If no access restrictions are present, then this will return the download url for the FileEntity FileHandle.  Otherwise, it will return null.
	 */
	public String getDirectDownloadURL();
	
	/**
	 * Presenter Interface 
	 */
	public interface Presenter {
		
		public boolean isDownloadAllowed();

		void clearHandlers();

		void addEntityUpdatedHandler(EntityUpdatedHandler handler);
		
		void showWindow();
	}

	public void showDownloadFailure();
	
	public void setNoDownloads();

	public void setUnauthorizedDownloads();

	public void setNeedToLogIn();
	
}
