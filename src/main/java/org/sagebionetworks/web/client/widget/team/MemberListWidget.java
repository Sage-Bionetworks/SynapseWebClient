package org.sagebionetworks.web.client.widget.team;


import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.TeamMemberPagedResults;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MemberListWidget implements MemberListWidgetView.Presenter {
	public static int MEMBER_LIMIT = 20;
	private int offset;
	private String searchTerm, teamId;
	private boolean isAdmin;
	private MemberListWidgetView view;
	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	private TeamMemberPagedResults memberList;
	private AuthenticationController authenticationController;
	private Callback teamUpdatedCallback;
	private SynapseAlert synAlert;
	private LoadMoreWidgetContainer membersContainer;
	
	@Inject
	public MemberListWidget(
			MemberListWidgetView view, 
			SynapseClientAsync synapseClient, 
			AuthenticationController authenticationController, 
			GlobalApplicationState globalApplicationState, 
			LoadMoreWidgetContainer membersContainer,
			SynapseAlert synAlert) {
		this.view = view;
		view.setPresenter(this);
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.synapseClient = synapseClient;
		this.membersContainer = membersContainer;
		this.synAlert = synAlert;
		view.setMembersContainer(membersContainer);
		membersContainer.configure(new Callback() {
			@Override
			public void invoke() {
				loadMore();
			}
		});
	}

	public void configure(String teamId, boolean isAdmin, Callback teamUpdatedCallback) {
		this.teamId = teamId;
		this.isAdmin = isAdmin;
		this.teamUpdatedCallback = teamUpdatedCallback;
		view.clearMembers();
		offset = 0;
		loadMore();
	};

	public void clear() {
		view.clearMembers();
	}
	
	public void refresh() {
		configure(teamId, isAdmin, teamUpdatedCallback);
	}
	
	public void loadMore() {
		synAlert.clear();
		synapseClient.getTeamMembers(teamId, searchTerm, MEMBER_LIMIT, offset, new AsyncCallback<TeamMemberPagedResults>() {
			@Override
			public void onSuccess(TeamMemberPagedResults results) {
				memberList = results;
				offset += MEMBER_LIMIT;
				
				long numberOfMembers = results.getTotalNumberOfResults();
				membersContainer.setIsMore(offset < numberOfMembers);
				view.addMembers(memberList.getResults(), isAdmin);
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	@Override
	public void removeMember(String principalId) {
		synAlert.clear();
		synapseClient.deleteTeamMember(authenticationController.getCurrentUserPrincipalId(), principalId, teamId, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				//success, refresh the team
				view.showInfo("Successfully removed the member");
				teamUpdatedCallback.invoke();			
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
	@Override
	public void setIsAdmin(String principalId, boolean isAdmin) {
		synAlert.clear();
		synapseClient.setIsTeamAdmin(authenticationController.getCurrentUserPrincipalId(), principalId, teamId, isAdmin, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				//success, refresh the team
				view.showInfo("Successfully updated the team member");
				teamUpdatedCallback.invoke();
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
				refresh();
			}
		});
	}
	
	@Override
	public void search(String searchTerm) {
		//New search term, and the offset must reset
		this.searchTerm = searchTerm;
		refresh();
	}
	
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}
}
