package org.sagebionetworks.web.client.presenter;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.repo.model.RSSEntry;
import org.sagebionetworks.repo.model.RSSFeed;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.RssServiceAsync;
import org.sagebionetworks.web.client.SearchServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.HomeView;
import org.sagebionetworks.web.client.widget.entity.browse.EntityBrowserUtils;
import org.sagebionetworks.web.shared.MembershipInvitationBundle;
import org.sagebionetworks.web.shared.exceptions.ConflictException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

@SuppressWarnings("unused")
public class HomePresenter extends AbstractActivity implements HomeView.Presenter, Presenter<Home> {
	public static final String KEY_DATASETS_SELECTED_COLUMNS_COOKIE = "org.sagebionetworks.selected.dataset.columns";

	private static final int MAX_NEWS_ITEMS = 3;
	
	private Home place;
	private HomeView view;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private RssServiceAsync rssService;
	private SearchServiceAsync searchService;
	private SynapseClientAsync synapseClient;
	private AdapterFactory adapterFactory;
	
	@Inject
	public HomePresenter(HomeView view,  
			AuthenticationController authenticationController, 
			GlobalApplicationState globalApplicationState,
			RssServiceAsync rssService,
			SearchServiceAsync searchService, 
			SynapseClientAsync synapseClient, 			
			AdapterFactory adapterFactory){
		this.view = view;
		// Set the presenter on the view
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.rssService = rssService;
		this.searchService = searchService;
		this.synapseClient = synapseClient;
		this.adapterFactory = adapterFactory;
		this.authenticationController = authenticationController;
		this.view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(Home place) {
		this.place = place;		
		view.setPresenter(this);		
		view.refresh();
		
		// Thing to load regardless of Authentication
		loadNewsFeed();
		
		// Things to load for authenticated users
		if(authenticationController.isLoggedIn()) {
			loadProjectsAndFavorites();
		}		
	}
		
	public void loadNewsFeed(){
		rssService.getCachedContent(ClientProperties.NEWS_FEED_PROVIDER_ID, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					view.showNews(getHtml(result));
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showNews("<p>"+DisplayConstants.NEWS_UNAVAILABLE_TEXT+"</p>");
			}
		});
	}
		
	public String getHtml(String rssFeedJson) throws JSONObjectAdapterException {		
		RSSFeed feed = new RSSFeed(adapterFactory.createNew(rssFeedJson));
		StringBuilder htmlResponse = new StringBuilder();
		int maxIdx = feed.getEntries().size() > MAX_NEWS_ITEMS ? MAX_NEWS_ITEMS : feed.getEntries().size();
		for (int i = 0; i < maxIdx; i++) {
			RSSEntry entry = feed.getEntries().get(i);
			//every max, set as the last
			String lastString = (i+1)%MAX_NEWS_ITEMS==0 ? "last" : "";
			htmlResponse.append("<div class=\"col-md-4 serv "+lastString+"\"><div class=\"icon-white-big left icon161-white\" style=\"background-color: rgb(122, 122, 122);\"></div><h5 style=\"margin-left: 25px;\"><a href=\"");
            htmlResponse.append(entry.getLink());
            htmlResponse.append("\" class=\"service-tipsy north link\">");
            htmlResponse.append(entry.getTitle());
            htmlResponse.append("</a></h5><p class=\"clear small-italic\">");
            htmlResponse.append(entry.getDate() + " - " + entry.getAuthor() + "<br>");
            htmlResponse.append(entry.getContent());
            htmlResponse.append("</p></div>");
		}
		return htmlResponse.toString();
	}

	public String getSupportFeedHtml(String rssFeedJson) throws JSONObjectAdapterException {
		RSSFeed feed = new RSSFeed(adapterFactory.createNew(rssFeedJson));
		StringBuilder htmlResponse = new StringBuilder();
		htmlResponse.append("<div> <ul class=\"list question-list\">");
		for (int i = 0; i < feed.getEntries().size(); i++) {
			RSSEntry entry = feed.getEntries().get(i);
			htmlResponse.append("<li style=\"padding-top: 0px; padding-bottom: 3px\"><h5 style=\"margin-bottom: 0px;\"><a href=\"");
            //all of the rss links are null from Get Satisfaction.  Just point each item to the main page, showing the recent activity
			//htmlResponse.append(entry.getLink());
			htmlResponse.append(ClientProperties.SUPPORT_RECENT_ACTIVITY_URL);
            htmlResponse.append("\" class=\"service-tipsy north link\">");
            htmlResponse.append(entry.getTitle());
            htmlResponse.append("</a></h5><p class=\"clear small-italic\" style=\"margin-bottom: 0px;\">");
            htmlResponse.append(entry.getAuthor());
            htmlResponse.append("</p></li>");
		}
		htmlResponse.append("</ul></div>");
		return htmlResponse.toString();
	}
	
	@Override
    public String mayStop() {
        view.clear();
        return null;
    }

	@Override
	public boolean showLoggedInDetails() {
		return authenticationController.isLoggedIn();
	}
	
	public void loadProjectsAndFavorites() {
		loadEvaluations(new AsyncCallback<List<EntityHeader>>() {
			@Override
			public void onSuccess(List<EntityHeader> result) {
				view.setMyEvaluationList(result);
			}
			@Override
			public void onFailure(Throwable caught) {
				view.setMyEvaluationsError("Could not load My Evaluations");
			}
		});
		
		view.refreshMyTeams(authenticationController.getCurrentUserPrincipalId());
		
		view.showOpenTeamInvitesMessage(false);
		isOpenTeamInvites(new CallbackP<Boolean>() {
			@Override
			public void invoke(Boolean b) {
				view.showOpenTeamInvitesMessage(b);
			}
		});
		
		
		EntityBrowserUtils.loadUserUpdateable(searchService, adapterFactory, globalApplicationState, authenticationController, new AsyncCallback<List<EntityHeader>>() {
			@Override
			public void onSuccess(List<EntityHeader> result) {
				view.setMyProjects(result);
			}
			@Override
			public void onFailure(Throwable caught) {
				view.setMyProjectsError("Could not load My Projects");
			}
		});
		
		EntityBrowserUtils.loadFavorites(synapseClient, adapterFactory, globalApplicationState, new AsyncCallback<List<EntityHeader>>() {
			@Override
			public void onSuccess(List<EntityHeader> result) {
				view.setFavorites(result);
			}
			@Override
			public void onFailure(Throwable caught) {
				view.setFavoritesError("Could not load Favorites");
			}
		});
	}
	
	public void isOpenTeamInvites(final CallbackP<Boolean> callback) {
		synapseClient.getOpenInvitations(authenticationController.getCurrentUserPrincipalId(), new AsyncCallback<List<MembershipInvitationBundle>>() {
			@Override
			public void onSuccess(List<MembershipInvitationBundle> result) {
				callback.invoke(result.size() > 0);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				//do nothing
			}
		});		
	}

	public void loadEvaluations(final AsyncCallback<List<EntityHeader>> callback){
		try {
			synapseClient.getAvailableEvaluationEntitiesList(new AsyncCallback<ArrayList<String>>() {
				@Override
				public void onSuccess(ArrayList<String> results) {					
					try {	
						List<EntityHeader> evals = new ArrayList<EntityHeader>();
						for(String eh : results) {
							evals.add(new EntityHeader(adapterFactory.createNew(eh)));
						}
						callback.onSuccess(evals);
					} catch (JSONObjectAdapterException e) {
						onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		} catch (RestServiceException e) {
			callback.onFailure(e);
		}

	}
	
	@Override
	public void createProject(final String name) {
		CreateEntityUtil.createProject(name, synapseClient, adapterFactory, globalApplicationState, authenticationController, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String newProjectId) {
				view.showInfo(DisplayConstants.LABEL_PROJECT_CREATED, name);
				globalApplicationState.getPlaceChanger().goTo(new Synapse(newProjectId));						
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(caught instanceof ConflictException) {
					view.showErrorMessage(DisplayConstants.WARNING_PROJECT_NAME_EXISTS);
				} else {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view)) {					
						view.showErrorMessage(DisplayConstants.ERROR_GENERIC_RELOAD);
					} 
				}
			}
		});
	}
	
	@Override
	public void createTeam(final String teamName) {
		synapseClient.createTeam(teamName, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String newTeamId) {
				view.showInfo(DisplayConstants.LABEL_TEAM_CREATED, teamName);
				globalApplicationState.getPlaceChanger().goTo(new org.sagebionetworks.web.client.place.Team(newTeamId));						
			}
			
			@Override
			public void onFailure(Throwable caught) {
//				if(caught instanceof ConflictException) {
//					view.showErrorMessage(DisplayConstants.WARNING_TEAM_NAME_EXISTS);
//				} else {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view)) {					
					view.showErrorMessage(caught.getMessage());
				}
//				}
			}
		});
	}
}
