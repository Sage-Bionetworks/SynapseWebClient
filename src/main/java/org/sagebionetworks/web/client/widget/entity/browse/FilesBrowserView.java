package org.sagebionetworks.web.client.widget.entity.browse;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.CallbackP;
import com.google.gwt.user.client.ui.IsWidget;

public interface FilesBrowserView extends IsWidget, SynapseView {

	/**
	 * Configure the view with the parent id
	 * 
	 * @param entityId
	 */
	void configure(String entityId);

	void setEntityClickedHandler(CallbackP<String> callback);

	void setPresenter(Presenter p);

	void setAddToDownloadList(IsWidget w);

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onProgrammaticDownloadOptions();

		void onAddToDownloadList();
	}
}
