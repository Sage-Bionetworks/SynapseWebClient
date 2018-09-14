package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import org.sagebionetworks.repo.model.ChallengeTeam;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EditRegisteredTeamDialog implements EditRegisteredTeamDialogView.Presenter {
	private EditRegisteredTeamDialogView view;
	private ChallengeClientAsync challengeClient;
	private Callback callback;
	private ChallengeTeam challengeTeam;
	@Inject
	public EditRegisteredTeamDialog(EditRegisteredTeamDialogView view, 
			ChallengeClientAsync challengeClient
			) {
		this.view = view;
		this.challengeClient = challengeClient;
		fixServiceEntryPoint(challengeClient);
		view.setPresenter(this);
	}		
	
	public Widget asWidget() {
		return view.asWidget();
	}
	
	private void clearState() {
		view.setRecruitmentMessage("");
		challengeTeam = null;
	}
	
	public void configure(ChallengeTeam challengeTeam, Callback callback) {
		clearState();
		this.challengeTeam = challengeTeam;
		this.callback = callback;
		if (challengeTeam.getMessage() != null)
			view.setRecruitmentMessage(challengeTeam.getMessage());
		view.showModal();
	}
	
	@Override
	public void onOk() {
		challengeTeam.setMessage(view.getRecruitmentMessage());
		challengeClient.updateRegisteredChallengeTeam(challengeTeam, new AsyncCallback<ChallengeTeam>() {
			@Override
			public void onSuccess(ChallengeTeam result) {
				if (callback != null) {
					callback.invoke();
				}
				view.hideModal();
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	@Override
	public void onUnregister() {
		challengeClient.unregisterChallengeTeam(challengeTeam.getId(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				view.showInfo("Successfully unregistered your team");
				if (callback != null) {
					callback.invoke();
				}
				view.hideModal();
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}
}
