package org.sagebionetworks.web.client.widget.team;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.shared.MembershipInvitationBundle;

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

	public void configure(final Callback teamUpdatedCallback, final CallbackP<List<MembershipInvitationBundle>> openTeamInvitationsCallback) {
		view.clear();
		//using the current user, ask for all of the open invitations extended to this user.
		if (authenticationController.isLoggedIn()) {
			//get the open invitations
			synapseClient.getOpenInvitations(authenticationController.getCurrentUserPrincipalId(), new AsyncCallback<ArrayList<MembershipInvitationBundle>>() {
				@Override
				public void onSuccess(ArrayList<MembershipInvitationBundle> result) {
					if (openTeamInvitationsCallback != null)
						openTeamInvitationsCallback.invoke(result);
					configure(teamUpdatedCallback, result);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)) {					
						view.showErrorMessage(caught.getMessage());
					} 
				}
			});			
		}
	};
	
	public void configure(Callback teamUpdatedCallback, List<MembershipInvitationBundle> invites) {
		this.teamUpdatedCallback = teamUpdatedCallback;
		try {
			//create the associated object list, and pass to the view to render
			List<Team> teams = new ArrayList<Team>();
			List<String> inviteMessages = new ArrayList<String>();
			for (MembershipInvitationBundle b : invites) {
				String invitationMessage = "";
				MembershipInvitation invite = nodeModelCreator.createJSONEntity(b.getMembershipInvitationJson(), MembershipInvitation.class);
				if (invite.getMessage() != null)
					invitationMessage = invite.getMessage();
				inviteMessages.add(invitationMessage);
				teams.add(nodeModelCreator.createJSONEntity(b.getTeamJson(), Team.class));
			}
			view.configure(teams, inviteMessages);
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
		}
	}

	
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
				//refresh the open invitations
				configure(teamUpdatedCallback, (CallbackP<List<MembershipInvitationBundle>>)null);
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
