package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.modal.Dialog;

import com.google.gwt.user.client.ui.IsWidget;

public interface UploadDialogWidgetView extends IsWidget, SynapseView {

	void setPresenter(Presenter presenter);
	void setUploadDialog(Dialog uploadDialog);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}
}
