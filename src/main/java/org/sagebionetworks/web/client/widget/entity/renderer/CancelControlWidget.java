package org.sagebionetworks.web.client.widget.entity.renderer;

import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.sagebionetworks.evaluation.model.CancelControl;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.security.AuthenticationController;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CancelControlWidget implements SingleButtonView.Presenter, IsWidget {
	
	private SingleButtonView view;
	private ChallengeClientAsync challengeClient;
	AuthenticationController authController;
	@Inject
	public CancelControlWidget(SingleButtonView view, 
			ChallengeClientAsync challengeClient,
			AuthenticationController authController) {
		this.view = view;
		this.challengeClient = challengeClient;
		this.authController = authController;
		view.setButtonText(DisplayConstants.BUTTON_CANCEL);
		view.setButtonType(ButtonType.DANGER);
		view.setPresenter(this);
	}
	
	public void configure(String json) {
		view.setButtonVisible(false);
		// try to construct the CancelControl object from the json
		CancelControl cancelControl;
		String submissionUserId = cancelControl.getUserId();
		if (authController.isLoggedIn() && authController.getCurrentUserPrincipalId().equals(submissionUserId)) {
			if (cancelControl.getCanCancel()) {
				view.setButtonVisible(true);
			}
		}
	}
	
	@Override
	public void onClick() {
		// request to cancel the submission
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
