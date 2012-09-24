package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;

import com.google.gwt.user.client.ui.IsWidget;

public interface LocationableUploaderView extends IsWidget, SynapseWidgetView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void createUploadForm(boolean showCancel);
	
	public void openNewTab(String url);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

		String getUploadActionUrl(boolean isRestricted);

		void setExternalLocation(String path);
		
		public void closeButtonSelected();

		/**
		 * 
		 * @param resultHtml
		 */
		void handleSubmitResult(String resultHtml, boolean isNewlyRestricted);
		
		/**
		 * returns true iff the dataset is currently (initially) restricted
		 * @return
		 */
		boolean isRestricted();

		String getJiraRestrictionLink();
	}


}
