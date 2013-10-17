package org.sagebionetworks.web.client.widget.team;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.repo.model.MembershipRequest;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.MembershipInvitationBundle;
import org.sagebionetworks.web.shared.MembershipRequestBundle;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OpenTeamInvitationsWidget implements OpenTeamInvitationsWidgetView.Presenter {
	private OpenTeamInvitationsWidgetView view;
	private GlobalApplicationState globalApplicationState;
	private NodeModelCreator nodeModelCreator;
	private SynapseClientAsync synapseClient;
	private Callback teamUpdatedCallback;
	private AuthenticationController authenticationController;
	
	@Inject
	public OpenTeamInvitationsWidget(OpenTeamInvitationsWidgetView view, 
			SynapseClientAsync synapseClient, 
			GlobalApplicationState globalApplicationState, 
			AuthenticationController authenticationController,
			NodeModelCreator nodeModelCreator) {
		this.view = view;
		view.setPresenter(this);
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.nodeModelCreator = nodeModelCreator;
	}

	public void configure(Callback teamUpdatedCallback) {
		view.clear();
		//using the current user, ask for all of the open invitations extended to this user.
		this.teamUpdatedCallback = teamUpdatedCallback;
		if (authenticationController.isLoggedIn()) {
			//get the open invitations
			synapseClient.getOpenInvitations(authenticationController.getCurrentUserPrincipalId(), new AsyncCallback<List<MembershipInvitationBundle>>() {
				@Override
				public void onSuccess(List<MembershipInvitationBundle> result) {
					try {
						//create the associated object list, and pass to the view to render
						List<Team> teams = new ArrayList<Team>();
						List<String> inviteMessages = new ArrayList<String>();
						for (MembershipInvitationBundle b : result) {
							String invitationMessage = "";
							MembershipInvitation invite = nodeModelCreator.createJSONEntity(b.getMembershipInvitationJson(), MembershipInvitation.class);
							if (invite.getMessage() != null)
								invitationMessage = invite.getMessage();
							inviteMessages.add(invitationMessage);
							teams.add(nodeModelCreator.createJSONEntity(b.getTeamJson(), Team.class));
						}
						view.configure(teams, inviteMessages);
					} catch (JSONObjectAdapterException e) {
						onFailure(e);
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view)) {					
						view.showErrorMessage(caught.getMessage());
					} 
				}
			});			
		}

	};

	
	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	@Override
	public void joinTeam(String teamId) {
		//issue join request for the selected team
		synapseClient.requestMembership(authenticationController.getCurrentUserPrincipalId(), teamId, "", new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				view.showInfo(DisplayConstants.JOIN_TEAM_SUCCESS, "");
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
