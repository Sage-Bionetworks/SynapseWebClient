package org.sagebionetworks.web.client.presenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.RSSEntry;
import org.sagebionetworks.repo.model.RSSFeed;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.RssServiceAsync;
import org.sagebionetworks.web.client.SearchServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.security.AuthenticationException;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.HomeView;
import org.sagebionetworks.web.client.widget.entity.browse.EntityBrowserUtils;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListUtil;
import org.sagebionetworks.web.client.widget.team.TeamListWidget;
import org.sagebionetworks.web.shared.MembershipInvitationBundle;
import org.sagebionetworks.web.shared.exceptions.ConflictException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

@SuppressWarnings("unused")
public class HomePresenter extends AbstractActivity implements HomeView.Presenter, Presenter<Home> {
	public static final String KEY_DATASETS_SELECTED_COLUMNS_COOKIE = "org.sagebionetworks.selected.dataset.columns";
	public static final String TEAMS_2_CHALLENGE_ENTITIES_COOKIE = "org.sagebionetworks.team.2.challenge.project";
	
	private static final int MAX_NEWS_ITEMS = 3;
	
	private Home place;
	private HomeView view;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private RssServiceAsync rssService;
	private SearchServiceAsync searchService;
	private SynapseClientAsync synapseClient;
	private AdapterFactory adapterFactory;
	private SynapseJSNIUtils synapseJSNIUtils;
	private GWTWrapper gwt;
	private RequestBuilderWrapper requestBuilder;
	private CookieProvider cookies;
	
	@Inject
	public HomePresenter(HomeView view,  
			AuthenticationController authenticationController, 
			GlobalApplicationState globalApplicationState,
			RssServiceAsync rssService,
			SearchServiceAsync searchService, 
			SynapseClientAsync synapseClient, 			
			AdapterFactory adapterFactory,
			SynapseJSNIUtils synapseJSNIUtils,
			GWTWrapper gwt,
			RequestBuilderWrapper requestBuilder,
			CookieProvider cookies){
		this.view = view;
		// Set the presenter on the view
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.rssService = rssService;
		this.searchService = searchService;
		this.synapseClient = synapseClient;
		this.adapterFactory = adapterFactory;
		this.authenticationController = authenticationController;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.gwt = gwt;
		this.requestBuilder = requestBuilder;
		this.cookies = cookies;
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
		
		checkAcceptToU();
		
		view.refresh();
		
		// Thing to load regardless of Authentication
		loadNewsFeed();
		
		// Things to load for authenticated users
		if(showLoggedInDetails()) {
			loadProjectsAndFavorites();
			//validate token
			validateToken();
		}
	}
		
	public void validateToken() {
		AsyncCallback<UserSessionData> callback = new AsyncCallback<UserSessionData>() {
			@Override
			public void onSuccess(UserSessionData result) {
				//do nothing
			}
			@Override
			public void onFailure(Throwable ex) {
				//token is invalid
				if (ex instanceof AuthenticationException) {
					// send user to login page						
					view.showInfo(DisplayConstants.SESSION_TIMEOUT, DisplayConstants.SESSION_HAS_TIMED_OUT);
					globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
				}
			}
		};
		UserSessionData userSessionData = authenticationController.getCurrentUserSessionData();
		if (userSessionData != null) {
			authenticationController.revalidateSession(authenticationController.getCurrentUserSessionToken(), callback);
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
	
	public void checkAcceptToU() {
		if (authenticationController.isLoggedIn() && !authenticationController.getCurrentUserSessionData().getSession().getAcceptsTermsOfUse()) {
			authenticationController.logoutUser();
		}
	}
	
	public void loadProjectsAndFavorites() {
		//ask for my teams
		TeamListWidget.getTeams(authenticationController.getCurrentUserPrincipalId(), synapseClient, adapterFactory, new AsyncCallback<List<Team>>() {
			@Override
			public void onFailure(Throwable caught) {
				view.setMyTeamsError("Could not load My Teams");
			}
			@Override
			public void onSuccess(List<Team> myTeams) {
				view.refreshMyTeams(myTeams);
				getChallengeProjectIds(myTeams);
			}
		});
		
		view.showOpenTeamInvitesMessage(false);
		isOpenTeamInvites(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean b) {
				view.showOpenTeamInvitesMessage(b);
			}
			@Override
			public void onFailure(Throwable caught) {
				//do nothing
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
	
	public void isOpenTeamInvites(final AsyncCallback<Boolean> callback) {
		if (!authenticationController.isLoggedIn()) { 
			callback.onSuccess(false);
			return;
		}
		synapseClient.getOpenInvitations(authenticationController.getCurrentUserPrincipalId(), new AsyncCallback<ArrayList<MembershipInvitationBundle>>() {
			@Override
			public void onSuccess(ArrayList<MembershipInvitationBundle> result) {
				callback.onSuccess(result.size() > 0);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});		
	}

	public void getChallengeProjectIds(final List<Team> myTeams) {
		getTeamId2ChallengeIdWhitelist(new CallbackP<JSONObjectAdapter>() {
			@Override
			public void invoke(JSONObjectAdapter mapping) {
				Set<String> challengeEntities = new HashSet<String>();
				for (Team team : myTeams) {
					if (mapping.has(team.getId())) {
						try {
							challengeEntities.add(mapping.getString(team.getId()));
						} catch (JSONObjectAdapterException e) {
							//problem with one of the mapping entries
						}
					}
				}
				getChallengeProjectHeaders(challengeEntities);
			}
		});
	}
	
	public void getChallengeProjectHeaders(final Set<String> challengeProjectIdsSet) {
		List<String> challengeProjectIds = new ArrayList<String>();
		challengeProjectIds.addAll(challengeProjectIdsSet);
		synapseClient.getEntityHeaderBatch(challengeProjectIds, new AsyncCallback<ArrayList<EntityHeader>>() {
			
			@Override
			public void onSuccess(ArrayList<EntityHeader> headers) {
				//finally, we can tell the view to update the user challenges based on these entity headers
				EntityBrowserUtils.sortEntityHeadersByName(headers);
				view.setMyChallenges(headers);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.setMyChallengesError("Could not load My Challenges:" + caught.getMessage());
			}
		});
	}
	
	public void getTeamId2ChallengeIdWhitelist(final CallbackP<JSONObjectAdapter> callback) {
		String responseText = cookies.getCookie(TEAMS_2_CHALLENGE_ENTITIES_COOKIE);
		
		if (responseText != null) {
			parseTeam2ChallengeWhitelist(responseText, callback);
			return;
		}
		requestBuilder.configure(RequestBuilder.GET, DisplayUtils.createRedirectUrl(synapseJSNIUtils.getBaseFileHandleUrl(), gwt.encodeQueryString(ClientProperties.TEAM2CHALLENGE_WHITELIST_URL)));
	     try
	     {
	    	 requestBuilder.sendRequest(null, new RequestCallback() {
	            @Override
	            public void onError(Request request, Throwable exception) 
	            {
	            	//do nothing, may or may not have any challenges
	            }

	            @Override
	            public void onResponseReceived(Request request,Response response) 
	            {
	            	String responseText = response.getText();
	            	Date expires = new Date(System.currentTimeMillis() + 1000*60*60*24); // store for a day
	            	cookies.setCookie(TEAMS_2_CHALLENGE_ENTITIES_COOKIE, responseText, expires);
	            	parseTeam2ChallengeWhitelist(responseText, callback);
	            }

	         });
	     }
	     catch (Exception e){
         	//failed to load my challenges
	    	 view.setMyChallengesError("Could not load My Challenges: " + e.getMessage());
	     }
	}
	
	private void parseTeam2ChallengeWhitelist(String responseText, CallbackP<JSONObjectAdapter> callback){
		try {
			callback.invoke(adapterFactory.createNew(responseText));
		} catch (Throwable e) {
			//just in case there is a parsing exception
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
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)) {					
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
				if(caught instanceof ConflictException) {
					view.showErrorMessage(DisplayConstants.WARNING_TEAM_NAME_EXISTS);
				} else {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)) {					
						view.showErrorMessage(caught.getMessage());
					}
				}
			}
		});
	}
}
