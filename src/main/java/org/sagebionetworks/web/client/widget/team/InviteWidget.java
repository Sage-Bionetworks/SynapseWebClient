package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class InviteWidget implements InviteWidgetView.Presenter {
	private InviteWidgetView view;
	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	private AuthenticationController authenticationController;
	private String teamId;
	private Callback teamUpdatedCallback;
	private GWTWrapper gwt;
	
	@Inject
	public InviteWidget(InviteWidgetView view, 
			SynapseClientAsync synapseClient, 
			AuthenticationController authenticationController, 
			GlobalApplicationState globalApplicationState,
			GWTWrapper gwt) {
		this.view = view;
		view.setPresenter(this);
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.gwt = gwt;
	}

	public void configure(String teamId, Callback teamUpdatedCallback) {
		//set team
		this.teamId = teamId;
		this.teamUpdatedCallback = teamUpdatedCallback;
		view.configure();
	};
	
	@Override
	public void sendInvitation(String principalId, String message, final String userDisplayName) {
		synapseClient.inviteMember(principalId, teamId, message, gwt.getHostPageBaseURL(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				view.showInfo("Invitation Sent", "The invitation has been sent to " + userDisplayName);
				teamUpdatedCallback.invoke();
			}
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)) {					
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
