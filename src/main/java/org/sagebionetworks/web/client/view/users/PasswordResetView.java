package org.sagebionetworks.web.client.view.users;

import org.sagebionetworks.web.client.SynapseView;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface PasswordResetView extends IsWidget, SynapseView {

	/**
	 * Set this view's presenter
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void showRequestForm();

	public void showResetForm();

	public void showPasswordResetRequired();

	public void showPasswordResetSuccess();

	public void showRequestSentSuccess();

	public void clear();

	void setSynAlertWidget(Widget w);

	void setSubmitButtonEnabled(boolean enabled);

	public interface Presenter {
		public void requestPasswordReset(String emailAddress);

		public void resetPassword(String currentPassword, String newPassword);
	}

	public void showExpiredRequest();

}
