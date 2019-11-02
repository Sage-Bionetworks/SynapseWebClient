package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;

public interface ImageWidgetView extends IsWidget {
	void configure(String url, String fileName, String scale, String alignment, String altText, String synapseId, boolean isLoggedIn);

	void setSynAlert(IsWidget w);

	void addStyleName(String style);

	void setPresenter(Presenter p);

	public interface Presenter {
		void handleLoadingError(String error);
	}

}
