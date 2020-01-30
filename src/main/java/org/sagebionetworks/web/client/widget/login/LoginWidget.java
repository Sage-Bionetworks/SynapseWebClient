package org.sagebionetworks.web.client.widget.login;

import org.sagebionetworks.web.client.security.AuthenticationController;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LoginWidget {

	private LoginWidgetView view;
	public static final String LOGIN_PLACE = "LoginPlace";
	private AuthenticationController authController;

	@Inject
	public LoginWidget(LoginWidgetView view, AuthenticationController authController) {
		this.view = view;
		this.authController = authController;
	}

	public Widget asWidget() {
		view.setVisible(!authController.isLoggedIn());
		return view.asWidget();
	}

	public void clear() {
		view.clear();
	}
}
