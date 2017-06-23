package org.sagebionetworks.web.client.widget.team;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.MembershipRequest;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.MembershipRequestBundle;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OpenMembershipRequestsWidget implements OpenMembershipRequestsWidgetView.Presenter {
	public static final String ACCEPTED_REQUEST_MESSAGE = "Accepted Request";
	private OpenMembershipRequestsWidgetView view;
	private GlobalApplicationState globalApplicationState;
	private Callback teamUpdatedCallback;
	private SynapseClientAsync synapseClient;
	private String teamId;
	private GWTWrapper gwt;
	private SynapseAlert synAlert;
	private PopupUtilsView popupUtils;
	
	@Inject
	public OpenMembershipRequestsWidget(OpenMembershipRequestsWidgetView view, 
			SynapseClientAsync synapseClient, 
			GlobalApplicationState globalApplicationState,
			GWTWrapper gwt,
			SynapseAlert synAlert,
			PopupUtilsView popupUtils) {
		this.view = view;
		this.popupUtils = popupUtils;
		this.synAlert = synAlert;
		view.setPresenter(this);
		view.setSynAlert(synAlert);
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.gwt = gwt;
	}

	public void configure(String teamId, Callback teamUpdatedCallback) {
		synAlert.clear();
		this.teamId = teamId;
		this.teamUpdatedCallback = teamUpdatedCallback;
		//using the given team, try to show all pending membership requests (or nothing if empty)
		synapseClient.getOpenRequests(teamId, new AsyncCallback<List<MembershipRequestBundle>>() {
			@Override
			public void onSuccess(List<MembershipRequestBundle> result) {
				//create the associated object list, and pass to the view to render
				List<UserProfile> profiles = new ArrayList<UserProfile>();
				List<String> requestMessages = new ArrayList<String>();
				for (MembershipRequestBundle b : result) {
					String requestMessage = "";
					MembershipRequest request = b.getMembershipRequest();
					if (request.getMessage() != null)
						requestMessage = request.getMessage();
					requestMessages.add(requestMessage);
					profiles.add(b.getUserProfile());
				}
				view.configure(profiles, requestMessages);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	};

	public void clear() {
		view.clear();
	}
	
	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	@Override
	public void acceptRequest(String userId) {
		synAlert.clear();
		//try to add user id to team (since we know there's an open membership request). then update open membership request list
		synapseClient.addTeamMember(userId, teamId, "", gwt.getHostPageBaseURL(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				popupUtils.showInfo(ACCEPTED_REQUEST_MESSAGE,"");
				teamUpdatedCallback.invoke();
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}
	
}
