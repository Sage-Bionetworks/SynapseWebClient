package org.sagebionetworks.web.client.widget.team;


import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import org.sagebionetworks.repo.model.TeamMemberTypeFilterOptions;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.shared.TeamMemberPagedResults;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MemberListWidget implements MemberListWidgetView.Presenter {
	public static int MEMBER_LIMIT = 24;
	private int offset;
	private String searchTerm, teamId;
	private TeamMemberTypeFilterOptions memberType;
	private boolean isAdmin;
	private MemberListWidgetView view;
	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	private SynapseJavascriptClient jsClient;
	private AuthenticationController authenticationController;
	private Callback teamUpdatedCallback;
	private LoadMoreWidgetContainer membersContainer;

	@Inject
	public MemberListWidget(MemberListWidgetView view, SynapseClientAsync synapseClient, SynapseJavascriptClient jsClient, AuthenticationController authenticationController, GlobalApplicationState globalApplicationState, LoadMoreWidgetContainer membersContainer) {
		this.view = view;
		view.setPresenter(this);
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.jsClient = jsClient;
		this.membersContainer = membersContainer;
		view.setMembersContainer(membersContainer);
		membersContainer.configure(new Callback() {
			@Override
			public void invoke() {
				loadMore();
			}
		});
	}

	public void configure(String teamId, boolean isAdmin, TeamMemberTypeFilterOptions memberType, Callback teamUpdatedCallback) {
		this.teamId = teamId;
		this.isAdmin = isAdmin;
		this.memberType = memberType;
		this.teamUpdatedCallback = teamUpdatedCallback;
		view.clearMembers();
		offset = 0;
		loadMore();
	};

	public void clear() {
		view.clearMembers();
	}

	public void refresh() {
		configure(teamId, isAdmin, memberType, teamUpdatedCallback);
	}

	public void loadMore() {
		jsClient.getTeamMembers(teamId, searchTerm, memberType, MEMBER_LIMIT, offset, new AsyncCallback<TeamMemberPagedResults>() {
			@Override
			public void onSuccess(TeamMemberPagedResults memberList) {
				offset += MEMBER_LIMIT;

				long numberOfMembers = memberList.getTotalNumberOfResults();
				membersContainer.setIsMore(offset < numberOfMembers);
				view.addMembers(memberList.getResults(), isAdmin);
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}

	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}

	@Override
	public void removeMember(String principalId) {
		jsClient.deleteTeamMember(teamId, principalId, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				// success, refresh the team
				view.showInfo("Successfully removed the member");
				teamUpdatedCallback.invoke();
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}

	@Override
	public void setIsAdmin(String principalId, boolean isAdmin) {
		synapseClient.setIsTeamAdmin(authenticationController.getCurrentUserPrincipalId(), principalId, teamId, isAdmin, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				// success, refresh the team
				view.showInfo("Successfully updated the team member");
				teamUpdatedCallback.invoke();
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
				refresh();
			}
		});
	}

	public void search(String searchTerm) {
		// New search term, and the offset must reset
		this.searchTerm = searchTerm;
		refresh();
	}

	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}
}
