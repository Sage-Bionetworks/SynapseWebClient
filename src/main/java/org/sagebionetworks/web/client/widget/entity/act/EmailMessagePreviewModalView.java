package org.sagebionetworks.web.client.widget.entity.act;

import com.google.gwt.user.client.ui.IsWidget;

public interface EmailMessagePreviewModalView extends IsWidget {

	void setPresenter(Presenter presenter);
	void setMessageBody(String message);
	void show();
	void hide();
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}

}
