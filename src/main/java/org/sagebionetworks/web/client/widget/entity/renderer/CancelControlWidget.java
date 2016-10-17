package org.sagebionetworks.web.client.widget.entity.renderer;

import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.extras.bootbox.client.callback.ConfirmCallback;
import org.sagebionetworks.evaluation.model.CancelControl;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CancelControlWidget implements SingleButtonView.Presenter, IsWidget {
	private SingleButtonView view;
	private ChallengeClientAsync challengeClient;
	AuthenticationController authController;
	Callback refreshRequiredCallback;
	SynapseAlert synAlert;
	JSONObjectAdapter jsonObjectAdapter;
	CancelControl cancelControl;
	public static final String CONFIRM_CANCEL = "Are you sure that you want to request that this be cancelled?";
	@Inject
	public CancelControlWidget(SingleButtonView view, 
			ChallengeClientAsync challengeClient,
			AuthenticationController authController, 
			SynapseAlert synAlert,
			JSONObjectAdapter jsonObjectAdapter) {
		this.view = view;
		this.challengeClient = challengeClient;
		this.authController = authController;
		this.synAlert = synAlert;
		this.jsonObjectAdapter = jsonObjectAdapter;
		view.setButtonText(DisplayConstants.BUTTON_CANCEL);
		view.setButtonType(ButtonType.DANGER);
		view.setPresenter(this);
		view.addWidget(synAlert.asWidget());
	}
	
	public void configure(String json, Callback refreshRequiredCallback) {
		view.setButtonVisible(false);
		synAlert.clear();
		try {
			// try to reconstruct the CancelControl object from the json
			cancelControl = new CancelControl();
			cancelControl.initializeFromJSONObject(jsonObjectAdapter.createNew(json));
			String submissionUserId = cancelControl.getUserId();
			if (authController.isLoggedIn() && authController.getCurrentUserPrincipalId().equals(submissionUserId)) {
				if (cancelControl.getCanCancel()) {
					view.setButtonVisible(true);
				}
			}
		} catch (JSONObjectAdapterException e) {
			synAlert.handleException(e);
		}
	}
	
	@Override
	public void onClick() {
		synAlert.clear();
		view.showConfirmDialog(CONFIRM_CANCEL, new ConfirmCallback() {
			@Override
			public void callback(boolean confirmed) {
				if (confirmed) {
					requestToCancelSubmission();
				}
			}
		});
	}
	
	public void requestToCancelSubmission() {
		// request to cancel the submission
		challengeClient.requestToCancelSubmission(cancelControl.getSubmissionId(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				refreshRequiredCallback.invoke();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
