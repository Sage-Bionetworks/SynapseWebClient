package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface FavoriteWidgetView extends IsWidget, SynapseView {

	void setPresenter(Presenter presenter);

	void setNotFavoriteVisible(boolean isVisible);
	void setFavoriteVisible(boolean isVisible);
	void hideFavoriteAndLoading();
	public interface Presenter {
		void favoriteClicked();
	}
}
