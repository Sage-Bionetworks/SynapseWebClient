package org.sagebionetworks.web.client.widget.header;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.widget.header.Header.MenuItems;

import com.google.gwt.user.client.ui.IsWidget;

public interface HeaderView extends IsWidget {
	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	public void setMenuItemActive(MenuItems menuItem);
	public void removeMenuItemActive(MenuItems menuItem);
	public void refresh();
	/**
	 * Sets the search box to visible or not
	 * @param searchVisible
	 */
	public void setSearchVisible(boolean searchVisible);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onTrashClick();
		void onLogoutClick();
		void onDashboardClick();
		void onLoginClick();
		void onRegisterClick();
		void onFavoriteClick();
		void onLogoClick();
	}
	public void showLargeLogo();
	public void showSmallLogo();
	public void clearFavorite();
	public void setEmptyFavorite();
	public void addFavorite(List<EntityHeader> headers);
	public void showFavoritesLoading();
	void setUser(UserSessionData userData);
	void setProjectHeaderText(String text);
	void setProjectHeaderAnchorTarget(String href);
	void setProjectFavoriteWidget(IsWidget favWidget);
	void showProjectFavoriteWidget();
	void hideProjectFavoriteWidget();
	void clear();
}
