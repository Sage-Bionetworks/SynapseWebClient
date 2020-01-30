package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.web.client.SynapseView;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface UploadDialogWidgetView extends IsWidget, SynapseView {

	void setPresenter(Presenter presenter);

	void configureDialog(String title, Widget body);

	void hideDialog();

	void showDialog();

	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}
}
