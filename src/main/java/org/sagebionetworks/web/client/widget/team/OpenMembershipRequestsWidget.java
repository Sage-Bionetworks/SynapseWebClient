package org.sagebionetworks.web.client.widget.team;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.MembershipRequest;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.MembershipRequestBundle;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OpenMembershipRequestsWidget implements OpenMembershipRequestsWidgetView.Presenter {

	private OpenMembershipRequestsWidgetView view;
	private GlobalApplicationState globalApplicationState;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private Callback teamUpdatedCallback;
	private SynapseClientAsync synapseClient;
	private String teamId;
	
	
	@Inject
	public OpenMembershipRequestsWidget(OpenMembershipRequestsWidgetView view, 
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

	public void configure(String teamId, Callback teamUpdatedCallback) {
		this.teamId = teamId;
		this.teamUpdatedCallback = teamUpdatedCallback;
		//using the given team, try to show all pending membership requests (or nothing if empty)
		synapseClient.getOpenRequests(teamId, new AsyncCallback<List<MembershipRequestBundle>>() {
			@Override
			public void onSuccess(List<MembershipRequestBundle> result) {
				try {
					//create the associated object list, and pass to the view to render
					List<UserProfile> profiles = new ArrayList<UserProfile>();
					List<String> requestMessages = new ArrayList<String>();
					for (MembershipRequestBundle b : result) {
						String requestMessage = "";
						MembershipRequest request = nodeModelCreator.createJSONEntity(b.getMembershipRequestJson(), MembershipRequest.class);
						if (request.getMessage() != null)
							requestMessage = request.getMessage();
						requestMessages.add(requestMessage);
						profiles.add(nodeModelCreator.createJSONEntity(b.getUserProfileJson(), UserProfile.class));
					}
					view.configure(profiles, requestMessages);
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
	
	@Override
	public void acceptRequest(String userId) {
		//invite user id to team (to complete handshake), then update open membership request list
		synapseClient.inviteMember(userId, teamId, "", new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				view.showInfo("Accepted Request","");
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
	
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}
	
}
