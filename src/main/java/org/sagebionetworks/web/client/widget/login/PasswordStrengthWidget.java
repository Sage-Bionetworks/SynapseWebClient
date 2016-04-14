package org.sagebionetworks.web.client.widget.login;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PasswordStrengthWidget implements PasswordStrengthWidgetView.Presenter {

	private PasswordStrengthWidgetView view;
	private int score;
	private String feedback;
	
	@Inject
	public PasswordStrengthWidget(PasswordStrengthWidgetView view) {
		this.view = view;
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
		if (password.length() < 8) {
			view.showWeakPasswordUI("Too short");	
		} else {
			_scorePassword(this, password);
			if (score == 0) {
				view.showWeakPasswordUI(feedback);
			} else if (score == 1) {
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
	
	/**
	 * 
	 * @param password
	 * @return
	 */
	private static native void _scorePassword(PasswordStrengthWidget x, String password) /*-{
		// Write instance field on x
		var result = $wnd.zxcvbn(password);
		x.@org.sagebionetworks.web.client.widget.login.PasswordStrengthWidget::score = result.score;
		if (result.score <= 2) {
			x.@org.sagebionetworks.web.client.widget.login.PasswordStrengthWidget::feedback = result.feedback.warning;
		}
	}-*/; 
}
