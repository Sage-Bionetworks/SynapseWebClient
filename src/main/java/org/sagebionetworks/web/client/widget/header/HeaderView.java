package org.sagebionetworks.web.client.widget.header;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.utils.Callback;
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
		void initUserFavorites(Callback callback);
	}

	public void setLargeLogo(boolean isHome);

	public void clearFavorite();

	public void setEmptyFavorite();

	public void addFavorite(List<EntityHeader> headers);

	void setUser(UserSessionData userData);


}
