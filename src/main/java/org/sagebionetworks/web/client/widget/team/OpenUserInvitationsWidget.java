package org.sagebionetworks.web.client.widget.team;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.shared.MembershipInvitationBundle;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OpenUserInvitationsWidget implements OpenUserInvitationsWidgetView.Presenter {

	private OpenUserInvitationsWidgetView view;
	private GlobalApplicationState globalApplicationState;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private SynapseClientAsync synapseClient;
	private String teamId;
	
	
	@Inject
	public OpenUserInvitationsWidget(OpenUserInvitationsWidgetView view, 
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

	public void configure(String teamId) {
		this.teamId = teamId;
		//using the given team, try to show all pending membership requests (or nothing if empty)
		synapseClient.getOpenTeamInvitations(teamId, new AsyncCallback<List<MembershipInvitationBundle>>() {
			@Override
			public void onSuccess(List<MembershipInvitationBundle> result) {
				try {
					//create the associated object list, and pass to the view to render
					List<UserProfile> profiles = new ArrayList<UserProfile>();
					List<String> invitationMessages = new ArrayList<String>();
					for (MembershipInvitationBundle b : result) {
						String requestMessage = "";
						MembershipInvitation invite = nodeModelCreator.createJSONEntity(b.getMembershipInvitationJson(), MembershipInvitation.class);
						if (invite.getMessage() != null)
							requestMessage = invite.getMessage();
						invitationMessages.add(requestMessage);
						profiles.add(nodeModelCreator.createJSONEntity(b.getUserProfileJson(), UserProfile.class));
					}
					view.configure(profiles, invitationMessages);
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
	};

	
	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}
	
}
