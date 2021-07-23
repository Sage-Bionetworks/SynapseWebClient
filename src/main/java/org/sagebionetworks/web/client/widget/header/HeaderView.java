package org.sagebionetworks.web.client.widget.header;

import org.sagebionetworks.schema.adapter.JSONObjectAdapter;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.event.shared.binder.EventBinder;

public interface HeaderView extends IsWidget {
	/**
	 * Set the presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void refresh();

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onCookieNotificationDismissed();
	}

	void setStagingAlertVisible(boolean visible);

	void openNewWindow(String url);

	void clear();

	EventBinder<Header> getEventBinder();

	void setCookieNotificationVisible(boolean visible);

	void setPortalAlertVisible(boolean visible, JSONObjectAdapter portalJson);
}
