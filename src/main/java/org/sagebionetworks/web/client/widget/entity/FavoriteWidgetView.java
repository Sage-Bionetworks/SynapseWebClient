package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface FavoriteWidgetView extends IsWidget, SynapseView {

	void setPresenter(Presenter presenter);

	void showIsFavorite();
	void showIsNotFavorite();
	void hideLoading();
	public interface Presenter {
		void favoriteClicked();
	}
}
