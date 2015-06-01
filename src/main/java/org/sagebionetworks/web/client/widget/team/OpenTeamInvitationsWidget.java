package org.sagebionetworks.web.client.widget.team;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.shared.OpenUserInvitationBundle;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OpenTeamInvitationsWidget implements OpenTeamInvitationsWidgetView.Presenter {
	private OpenTeamInvitationsWidgetView view;
	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	private AuthenticationController authenticationController;
	private PortalGinInjector ginInjector;
	private Callback teamUpdatedCallback, refreshCallback;
	
	@Inject
	public OpenTeamInvitationsWidget(OpenTeamInvitationsWidgetView view, 
			SynapseClientAsync synapseClient, 
			GlobalApplicationState globalApplicationState, 
			AuthenticationController authenticationController,
			PortalGinInjector ginInjector
			) {
		this.view = view;
		view.setPresenter(this);
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.ginInjector = ginInjector;
		this.refreshCallback = new Callback() {
			@Override
			public void invoke() {
				refresh();
			}
		};
	}

	public void configure(final Callback teamUpdatedCallback, final CallbackP<List<OpenUserInvitationBundle>> openTeamInvitationsCallback) {
		this.teamUpdatedCallback = teamUpdatedCallback;
		
		view.clear();
		//using the current user, ask for all of the open invitations extended to this user.
		if (authenticationController.isLoggedIn()) {
			//get the open invitations
			synapseClient.getOpenInvitations(authenticationController.getCurrentUserPrincipalId(), new AsyncCallback<ArrayList<OpenUserInvitationBundle>>() {
				@Override
				public void onSuccess(ArrayList<OpenUserInvitationBundle> result) {
					if (openTeamInvitationsCallback != null)
						openTeamInvitationsCallback.invoke(result);
					showTeamInvites(result);
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
	
	/**
	 * Update the view to display the given team invitations, with a Join button for each team.
	 * @param invites
	 */
	public void showTeamInvites(List<OpenUserInvitationBundle> invites) {
		//create the associated object list, and pass to the view to render
		view.clear();
		for (OpenUserInvitationBundle b : invites) {
			String invitationMessage = "";
			MembershipInvitation invite = b.getMembershipInvitation();
			if (invite.getMessage() != null) {
				invitationMessage = invite.getMessage();
			}
			JoinTeamWidget joinButton = ginInjector.getJoinTeamWidget();
			joinButton.configure(b.getTeam().getId(), refreshCallback);
			view.addTeamInvite(b.getTeam(), invitationMessage, joinButton.asWidget());
		}
	}
	
	public void refresh() {
		if (teamUpdatedCallback != null) {
			teamUpdatedCallback.invoke();	
		}
		configure(teamUpdatedCallback, (CallbackP<List<OpenUserInvitationBundle>>)null);
	}
	
	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	public void clear() {
		view.clear();
	}
	
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}

	

}
