package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.web.client.widget.SynapseWidgetView;

import com.google.gwt.user.client.ui.IsWidget;

public interface UploaderView extends IsWidget, SynapseWidgetView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void createUploadForm(boolean isExternalSupported);
	
	public void openNewBrowserTab(String url);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

		String getUploadActionUrl(boolean isRestricted);

		void setExternalLocation(String path, boolean isNewlyRestricted);
		
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

		void clearHandlers();
	}


}
