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
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OpenUserInvitationsWidget implements OpenUserInvitationsWidgetView.Presenter {

	public static final Integer INVITATION_BATCH_LIMIT = 10;
	private OpenUserInvitationsWidgetView view;
	private GlobalApplicationState globalApplicationState;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private SynapseClientAsync synapseClient;
	private String teamId;
	private Callback teamRefreshCallback;
	private Integer currentOffset;
	private List<UserProfile> profiles;
	private List<MembershipInvtnSubmission> invitations;
	
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
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)) {					
					view.showErrorMessage(caught.getMessage());
				} 
			}
		});
	}
	
	public void configure(String teamId, Callback teamRefreshCallback) {
		this.teamId = teamId;
		this.teamRefreshCallback = teamRefreshCallback;
		profiles = new ArrayList<UserProfile>();
		invitations = new ArrayList<MembershipInvtnSubmission>();
		currentOffset = 0;
		getNextBatch();
	};

	@Override
	public void getNextBatch() {
		//using the given team, try to show all pending membership requests (or nothing if empty)
		synapseClient.getOpenTeamInvitations(teamId, INVITATION_BATCH_LIMIT, currentOffset, new AsyncCallback<ArrayList<MembershipInvitationBundle>>() {
			@Override
			public void onSuccess(ArrayList<MembershipInvitationBundle> result) {
				try {
					currentOffset += result.size();
					
					//create the associated object list, and pass to the view to render
					for (MembershipInvitationBundle b : result) {
						invitations.add(nodeModelCreator.createJSONEntity(b.getMembershipInvitationJson(), MembershipInvtnSubmission.class));
						profiles.add(nodeModelCreator.createJSONEntity(b.getUserProfileJson(), UserProfile.class));
					}
					view.configure(profiles, invitations);
					
					//show the more button if we maxed out the return results
					view.setMoreResultsVisible(result.size() == INVITATION_BATCH_LIMIT);
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof NotFoundException) {
					view.setMoreResultsVisible(false);
				} else if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)) {					
					view.showErrorMessage(caught.getMessage());
				} 
			}
		});
	}
	
	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}
	
}
