package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface PasswordResetSignedTokenView extends IsWidget {
	void setPresenter(Presenter presenter);

	void showPasswordChangeSuccess();

	void clear();

	void setSynAlertWidget(IsWidget synAlert);

	String getPassword1Field();

	String getPassword2Field();

	void setChangePasswordEnabled(boolean isEnabled);

	public interface Presenter {
		void onChangePassword();
	}
}
