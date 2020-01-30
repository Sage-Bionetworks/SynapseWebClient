package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;

public interface FavoriteWidgetView extends IsWidget {

	void setPresenter(Presenter presenter);

	void setNotFavoriteVisible(boolean isVisible);

	void setFavoriteVisible(boolean isVisible);

	public interface Presenter {
		void favoriteClicked();
	}

	void setFavWidgetContainerVisible(boolean isVisible);

	void setLoadingVisible(boolean isVisible);

	void setLoadingSize(int px);

	void showErrorMessage(String errorSaveFavoriteMessage);
}
