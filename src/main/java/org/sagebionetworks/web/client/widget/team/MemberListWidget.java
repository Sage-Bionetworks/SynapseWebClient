package org.sagebionetworks.web.client.widget.team;


import java.util.List;

import org.sagebionetworks.repo.model.TeamMember;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;
import org.sagebionetworks.web.client.widget.search.PaginationUtil;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.TeamMemberPagedResults;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MemberListWidget implements MemberListWidgetView.Presenter {
	public static int MEMBER_LIMIT = 100;
	private int offset;
	private String searchTerm, teamId;
	private boolean isAdmin;
	private MemberListWidgetView view;
	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private TeamMemberPagedResults memberList;
	private AuthenticationController authenticationController;
	private Callback teamUpdatedCallback;
	
	@Inject
	public MemberListWidget(
			MemberListWidgetView view, 
			SynapseClientAsync synapseClient, 
			AuthenticationController authenticationController, 
			GlobalApplicationState globalApplicationState, 
			NodeModelCreator nodeModelCreator,
			JSONObjectAdapter jsonObjectAdapter) {
		this.view = view;
		view.setPresenter(this);
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
	}

	public void configure(String teamId, String initialSearchTerm, int initialOffset, boolean isAdmin, Callback teamUpdatedCallback) {
		this.teamId = teamId;
		this.isAdmin = isAdmin;
		this.teamUpdatedCallback = teamUpdatedCallback;
		refreshMembers(initialSearchTerm, initialOffset);
	};
	
	public void configure(String teamId, boolean isAdmin, Callback teamUpdatedCallback) {
		configure(teamId, null, 0, isAdmin, teamUpdatedCallback);
	};
	
	public void refreshMembers(final String searchTerm, int offset) {
		this.searchTerm = searchTerm;
		this.offset = offset;
		
		synapseClient.getTeamMembers(teamId, searchTerm, MEMBER_LIMIT, offset, new AsyncCallback<TeamMemberPagedResults>() {
			@Override
			public void onSuccess(TeamMemberPagedResults results) {
				memberList = results;
				view.configure(memberList.getResults(), searchTerm, isAdmin);
			}
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)) {					
					view.showErrorMessage(caught.getMessage());
				} 
			}
		});
	}
	
	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	@Override
	public void removeMember(String principalId) {
		synapseClient.deleteTeamMember(authenticationController.getCurrentUserPrincipalId(), principalId, teamId, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				//success, refresh the team
				view.showInfo("Successfully removed the member", "");
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
	
	@Override
	public void setIsAdmin(String principalId, boolean isAdmin) {
		synapseClient.setIsTeamAdmin(authenticationController.getCurrentUserPrincipalId(), principalId, teamId, isAdmin, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				//success, refresh the team
				view.showInfo("Successfully updated the team member", "");
				teamUpdatedCallback.invoke();			
			}
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)) {					
					view.showErrorMessage(caught.getMessage());
				}
				refreshMembers(searchTerm, offset);
			}
		});
	}

	@Override
	public void jumpToOffset(int offset) {
		//everything remains the same except for the offset
		refreshMembers(searchTerm, offset);
	}

	@Override
	public void search(String searchTerm) {
		//New search term, and the offset must reset
		refreshMembers(searchTerm, 0);
	}
	
	@Override
	public List<PaginationEntry> getPaginationEntries(int nPerPage, int nPagesToShow) {
		Long nResults = memberList.getTotalNumberOfResults();
		if(nResults == null)
			return null;
		return PaginationUtil.getPagination(nResults.intValue(), offset, nPerPage, nPagesToShow);
	}
	
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}
}
