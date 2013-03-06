package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.widget.SynapseWidgetView;

import com.google.gwt.user.client.ui.IsWidget;

public interface FavoriteWidgetView extends IsWidget, SynapseWidgetView {

	void setPresenter(Presenter presenter);

	void showIsFavorite(boolean isFavorite);

	public interface Presenter {

		void setIsFavorite(boolean isFavorite);
		
	}

}
