package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.TeamMembershipState;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class JoinTeamWidget implements JoinTeamWidgetView.Presenter {
	private JoinTeamWidgetView view;
	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	private String teamId;
	private AuthenticationController authenticationController; 
	private Callback teamUpdatedCallback;
	
	@Inject
	public JoinTeamWidget(JoinTeamWidgetView view, SynapseClientAsync synapseClient, GlobalApplicationState globalApplicationState, AuthenticationController authenticationController) {
		this.view = view;
		view.setPresenter(this);
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
	}

	public void configure(String teamId, TeamMembershipState membershipState, Callback teamUpdatedCallback) {
		//set team id
		this.teamId = teamId;
		this.teamUpdatedCallback = teamUpdatedCallback;
		view.configure(authenticationController.isLoggedIn(), membershipState);
	};
//	
//	@Override
//	public void deleteAllJoinRequests() {
//		synapseClient.deleteOpenMembershipRequests(authenticationController.getCurrentUserPrincipalId(), teamId, new AsyncCallback<Void>() {
//			@Override
//			public void onSuccess(Void result) {
//				view.showInfo("Cancelled Request", "The request to join the team has been removed.");
//				teamUpdatedCallback.invoke();
//			}
//			@Override
//			public void onFailure(Throwable caught) {
//				if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view)) {					
//					view.showErrorMessage(caught.getMessage());
//				} 
//			}
//		});
//	}
	
	@Override
	public void sendJoinRequest(String message, final boolean isAcceptingInvite) {
		synapseClient.requestMembership(authenticationController.getCurrentUserPrincipalId(), teamId, message, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				String message = isAcceptingInvite ? "Invitation Accepted" : "Request Sent";
				view.showInfo(message, "");
				teamUpdatedCallback.invoke();
			}
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view)) {					
					view.showErrorMessage(caught.getMessage());
				} 
			}
		});
	}
	
	public void clear() {
		view.clear();
	}
	
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}
}
