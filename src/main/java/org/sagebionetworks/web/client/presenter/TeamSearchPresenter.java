package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.TeamSearch;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.TeamSearchView;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.team.BigTeamBadge;
import org.sagebionetworks.web.shared.PaginatedResults;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class TeamSearchPresenter extends AbstractActivity implements TeamSearchView.Presenter, Presenter<TeamSearch> {
	public static int SEARCH_TEAM_LIMIT = 30;

	private TeamSearch place;
	private TeamSearchView view;
	private SynapseClientAsync synapseClient;
	private GlobalApplicationState globalApplicationState;
	private String searchTerm;
	private int offset;
	private SynapseAlert synAlert;
	private LoadMoreWidgetContainer loadMoreWidgetContainer;
	private PortalGinInjector ginInjector;

	@Inject
	public TeamSearchPresenter(TeamSearchView view, GlobalApplicationState globalApplicationState, SynapseClientAsync synapseClient, CookieProvider cookieProvider, SynapseAlert synAlert, LoadMoreWidgetContainer loadMoreWidgetContainer, PortalGinInjector ginInjector) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.synAlert = synAlert;
		this.loadMoreWidgetContainer = loadMoreWidgetContainer;
		this.ginInjector = ginInjector;
		view.setSynAlertWidget(synAlert.asWidget());
		view.setPresenter(this);
		loadMoreWidgetContainer.addStyleName("SRC-card-grid-row");
		loadMoreWidgetContainer.configure(new Callback() {
			@Override
			public void invoke() {
				loadMore();
			}
		});
		view.setLoadMoreContainer(loadMoreWidgetContainer.asWidget());
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
		searchTerm = place.getSearchTerm();
		view.setSearchTerm(searchTerm);
		offset = place.getStart() == null ? 0 : place.getStart();
		loadMoreWidgetContainer.clear();
		loadMore();
	}

	@Override
	public String mayStop() {
		return null;
	}


	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}

	public void loadMore() {
		synAlert.clear();
		// execute search, and update view with the results
		AsyncCallback<PaginatedResults<Team>> callback = new AsyncCallback<PaginatedResults<Team>>() {
			@Override
			public void onSuccess(PaginatedResults<Team> result) {
				for (Team team : result.getResults()) {
					BigTeamBadge teamBadge = ginInjector.getBigTeamBadgeWidget();
					teamBadge.configure(team, "");
					teamBadge.addStyleName("light-border SRC-grid-item");
					loadMoreWidgetContainer.add(teamBadge.asWidget());
				}
				offset += SEARCH_TEAM_LIMIT;
				loadMoreWidgetContainer.setIsMore(!result.getResults().isEmpty());
			}

			@Override
			public void onFailure(Throwable caught) {
				loadMoreWidgetContainer.setIsMore(false);
				synAlert.handleException(caught);
			}
		};
		synapseClient.getTeamsBySearch(searchTerm, SEARCH_TEAM_LIMIT, offset, callback);
	}

	public static boolean getCanPublicJoin(Team team) {
		return team.getCanPublicJoin() == null ? false : team.getCanPublicJoin();
	}

}
