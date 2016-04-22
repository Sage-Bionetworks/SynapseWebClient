package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.pagination.DetailedPaginationWidget;
import org.sagebionetworks.web.client.widget.pagination.PageChangeListener;
import org.sagebionetworks.web.shared.TeamMemberBundle;
import org.sagebionetworks.web.shared.TeamMemberPagedResults;
import org.sagebionetworks.web.shared.UserProfilePagedResults;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamMembersWidget implements UserListView.Presenter, WidgetRendererPresenter, PageChangeListener {
	
	private UserListView view;
	private Map<String,String> descriptor;
	private SynapseClientAsync synapseClient;
	private String teamId;
	
	private DetailedPaginationWidget paginationWidget;
	public static final Long DEFAULT_USER_LIMIT = 50L;
	public static final Long DEFAULT_OFFSET = 0L;
	
	@Inject
	public TeamMembersWidget(UserListView view, 
			DetailedPaginationWidget paginationWidget, 
			SynapseClientAsync synapseClient) {
		this.view = view;
		this.paginationWidget = paginationWidget;
		this.synapseClient = synapseClient;
		view.setPaginationWidget(paginationWidget.asWidget());
		view.setPresenter(this);
	}
	
	@Override
	public void configure(final WikiPageKey wikiKey, final Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		this.descriptor = widgetDescriptor;
		teamId = descriptor.get(WidgetConstants.TEAM_ID_KEY);
		descriptor = widgetDescriptor;
		if (teamId == null) {
			view.showErrorMessage(WidgetConstants.TEAM_ID_KEY + " is required.");
		} else {
			//get the team members
			onPageChange(DEFAULT_OFFSET);
		}
	}
	
	@Override
	public void onPageChange(final Long newOffset) {
		view.hideErrors();
		view.showLoading();
		view.clearUsers();
		synapseClient.getTeamMembers(teamId, "", DEFAULT_USER_LIMIT.intValue(), newOffset.intValue(), new AsyncCallback<TeamMemberPagedResults>() {
			@Override
			public void onSuccess(TeamMemberPagedResults results) {
				view.hideLoading();
				if (results.getTotalNumberOfResults() > 0) {
					//configure the pager, and the participant list
					paginationWidget.configure(DEFAULT_USER_LIMIT, newOffset, results.getTotalNumberOfResults(), TeamMembersWidget.this);
					for (TeamMemberBundle bundle : results.getResults()) {
						view.addUser(bundle.getUserProfile());
					}
				} else {
					view.showNoUsers();
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				view.hideLoading();
				view.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
