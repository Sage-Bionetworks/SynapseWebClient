package org.sagebionetworks.web.client.presenter;

import java.util.List;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.TeamSearch;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.TeamSearchView;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;
import org.sagebionetworks.web.client.widget.search.PaginationUtil;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class TeamSearchPresenter extends AbstractActivity implements TeamSearchView.Presenter, Presenter<TeamSearch> {
	public static int SEARCH_TEAM_LIMIT = 10;
	
	private TeamSearch place;
	private TeamSearchView view;
	private SynapseClientAsync synapseClient;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private int offset;
	private String searchTerm;
	private PaginatedResults<Team> teamList;
	
	@Inject
	public TeamSearchPresenter(TeamSearchView view,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			CookieProvider cookieProvider) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(TeamSearch place) {
		this.place = place;
		this.view.setPresenter(this);
		this.view.clear();
		showView(place);
	}
	
	@Override
    public String mayStop() {
        view.clear();
        return null;
    }
	

	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	@Override
	public void search(final String searchTerm, final Integer offset) {
		this.searchTerm = searchTerm;
		if (offset == null)
			this.offset = 0;
		else
			this.offset = offset;
		//execute search, and update view with the results
		synapseClient.getTeamsBySearch(searchTerm, SEARCH_TEAM_LIMIT, offset, new AsyncCallback<PaginatedResults<Team>>() {
			@Override
			public void onSuccess(PaginatedResults<Team> result) {
				teamList = result;
				view.configure(teamList.getResults(), searchTerm);	
				if (teamList.getResults().isEmpty()) {
					view.showEmptyTeams();
				}
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
	public List<PaginationEntry> getPaginationEntries(int nPerPage, int nPagesToShow) {
		Long nResults = teamList.getTotalNumberOfResults();
		if(nResults == null)
			return null;
		return PaginationUtil.getPagination(nResults.intValue(), offset, nPerPage, nPagesToShow);
	}
	
	@Override
	public int getOffset() {
		return offset;
	}
	
	private void showView(TeamSearch place) {
		String searchTerm = place.getSearchTerm();
		Integer offset = place.getStart();
		search(searchTerm, offset);
	}

	public static boolean getCanPublicJoin(Team team) {
		return team.getCanPublicJoin() == null ? false : team.getCanPublicJoin();
	}

}
