package org.sagebionetworks.web.client.view.users;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface PasswordResetView extends IsWidget, SynapseView {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
		
	public void showRequestForm();
	
	public void showResetForm();
	
	public void showMessage(String message);
	
	public void showPasswordResetSuccess();
	
	public void showRequestSentSuccess();
		
	public void clear();
	void setPasswordStrengthWidget(Widget w);
	public interface Presenter {
		
		public void requestPasswordReset(String emailAddress);
		
		public void resetPassword(String newPassword);
		public void passwordChanged(String password);
	}

	public void showExpiredRequest();

}
