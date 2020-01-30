package org.sagebionetworks.web.client.widget.profile;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * 
 * @author Jay
 *
 */
public interface EmailAddressesWidgetView extends IsWidget {

	void setVisible(boolean visible);

	void setSynAlert(IsWidget w);

	void setLoadingVisible(boolean visible);

	void addPrimaryEmail(String email, boolean isQuarantined);

	void addSecondaryEmail(String email);

	void clearEmails();

	public interface Presenter {
		void onAddEmail(String email);

		void onRemoveEmail(String email);

		void onMakePrimary(String email);
	}

	void setPresenter(Presenter presenter);
}
