package org.sagebionetworks.web.client.widget.team;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.MembershipInvtnSubmission;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
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
	private Callback teamRefreshCallback;
	
	
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

	@Override
	public void removeInvitation(String invitationId) {
		synapseClient.deleteMembershipInvitation(invitationId, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				teamRefreshCallback.invoke();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view)) {					
					view.showErrorMessage(caught.getMessage());
				} 
			}
		});
	}
	public void configure(String teamId, Callback teamRefreshCallback) {
		this.teamId = teamId;
		this.teamRefreshCallback = teamRefreshCallback;
		
		//using the given team, try to show all pending membership requests (or nothing if empty)
		synapseClient.getOpenTeamInvitations(teamId, new AsyncCallback<List<MembershipInvitationBundle>>() {
			@Override
			public void onSuccess(List<MembershipInvitationBundle> result) {
				try {
					//create the associated object list, and pass to the view to render
					List<UserProfile> profiles = new ArrayList<UserProfile>();
					List<MembershipInvtnSubmission> invitations = new ArrayList<MembershipInvtnSubmission>();
					for (MembershipInvitationBundle b : result) {
						invitations.add(nodeModelCreator.createJSONEntity(b.getMembershipInvitationJson(), MembershipInvtnSubmission.class));
						profiles.add(nodeModelCreator.createJSONEntity(b.getUserProfileJson(), UserProfile.class));
					}
					view.configure(profiles, invitations);
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
