package org.sagebionetworks.web.client.widget.header;

import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.widget.header.Header.MenuItems;

import com.google.gwt.user.client.rpc.AsyncCallback;
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
		UserSessionData getUser();
		void getSupportHRef(AsyncCallback<String> callback);
		void lookupId(String synapseId);
	}


}
