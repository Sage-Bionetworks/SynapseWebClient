package org.sagebionetworks.web.client.widget.login;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PasswordStrengthWidget implements PasswordStrengthWidgetView.Presenter {

	public static final int MAX_PASSWORD_TEST_LENGTH = 100;
	public static final int MIN_PASSWORD_LENGTH = 8;
	public static final String TOO_SHORT_MESSAGE = "Too short";
	private PasswordStrengthWidgetView view;
	private ZxcvbnWrapper zxcvbn;
	@Inject
	public PasswordStrengthWidget(PasswordStrengthWidgetView view, ZxcvbnWrapper zxcvbn) {
		this.view = view;
		this.zxcvbn = zxcvbn;
		view.setPresenter(this);
	}

	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void setVisible(boolean visible) {
		view.setVisible(visible);
	}
	
	public void scorePassword(String password) {
		if (password == null || password.length() == 0) {
			view.setVisible(false);
			return;
		}
		if (password.length() < MIN_PASSWORD_LENGTH) {
			view.showWeakPasswordUI(TOO_SHORT_MESSAGE);	
		} else {
			if (password.length() > MAX_PASSWORD_TEST_LENGTH) {
				//only process the first 100 characters
				password = password.substring(0, MAX_PASSWORD_TEST_LENGTH);
			}
			zxcvbn.scorePassword(password);
			int score = zxcvbn.getScore();
			String feedback = zxcvbn.getFeedback();
			if (score < 2) {
				view.showWeakPasswordUI(feedback);
			} else if (score == 2) {
				view.showFairPasswordUI(feedback);
			} else if (score == 3) {
				view.showGoodPasswordUI();
			} else if (score == 4) {
				view.showStrongPasswordUI();
			}
		}
		view.setVisible(true);
	};
	
	 
}
