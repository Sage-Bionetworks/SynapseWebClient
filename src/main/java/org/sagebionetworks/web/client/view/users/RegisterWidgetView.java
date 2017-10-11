package org.sagebionetworks.web.client.view.users;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.sagebionetworks.repo.model.MembershipInvtnSignedToken;
import org.sagebionetworks.repo.model.auth.NewUser;

public interface RegisterWidgetView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	void setSynAlert(Widget w);
	/**
	 * Presenter interface
	 */
	interface Presenter {
		void registerUser(NewUser newUser);

		MembershipInvtnSignedToken getMembershipInvtnSignedToken();
	}
	void enableRegisterButton(boolean enable);
	void setVisible(boolean isVisible);

	void setEmail(String email);
	void clear();
	void showInfo(String message);
}
