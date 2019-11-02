package org.sagebionetworks.web.client.widget.entity.renderer;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.sagebionetworks.evaluation.model.CancelControl;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.ServiceEntryPointUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CancelControlWidget implements SingleButtonView.Presenter, IsWidget {
	private SingleButtonView view;
	private ChallengeClientAsync challengeClient;
	AuthenticationController authController;
	SynapseAlert synAlert;
	AdapterFactory adapterFactory;
	CancelControl cancelControl;

	public static final String CONFIRM_CANCEL = "Are you sure that you want to request that this be cancelled?";

	@Inject
	public CancelControlWidget(SingleButtonView view, ChallengeClientAsync challengeClient, AuthenticationController authController, SynapseAlert synAlert, AdapterFactory adapterFactory) {
		this.view = view;
		this.challengeClient = challengeClient;
		ServiceEntryPointUtils.fixServiceEntryPoint(challengeClient);
		this.authController = authController;
		this.synAlert = synAlert;
		this.adapterFactory = adapterFactory;
		view.setButtonText(DisplayConstants.BUTTON_CANCEL);
		view.setDataLoadingText(DisplayConstants.BUTTON_CANCEL_REQUESTED);
		view.setButtonType(ButtonType.DANGER);
		view.setButtonSize(ButtonSize.EXTRA_SMALL);
		view.setPresenter(this);
		view.addWidget(synAlert.asWidget());
	}

	public void configure(String json) {
		view.setButtonVisible(false);
		view.setLoading(false);
		synAlert.clear();
		try {
			// try to reconstruct the CancelControl object from the json
			cancelControl = new CancelControl();
			cancelControl.initializeFromJSONObject(adapterFactory.createNew(json));
			String submissionUserId = cancelControl.getUserId();
			if (authController.isLoggedIn() && authController.getCurrentUserPrincipalId().equals(submissionUserId)) {
				if (cancelControl.getCanCancel()) {
					view.setButtonVisible(true);
					if (cancelControl.getCancelRequested()) {
						view.setLoading(true);
					}
				}
			}
		} catch (JSONObjectAdapterException e) {
			synAlert.handleException(e);
		}
	}

	@Override
	public void onClick() {
		synAlert.clear();
		view.showConfirmDialog(CONFIRM_CANCEL, () -> {
			requestToCancelSubmission();
		});
	}

	public void requestToCancelSubmission() {
		// request to cancel the submission
		// put into a processing state
		view.setLoading(true);
		challengeClient.requestToCancelSubmission(cancelControl.getSubmissionId(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				// cancel successfully requested
				view.setLoading(true);
			}

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
				view.setLoading(false);
			}
		});
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
