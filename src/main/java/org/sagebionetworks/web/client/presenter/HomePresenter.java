package org.sagebionetworks.web.client.presenter;

import java.util.Iterator;

import org.sagebionetworks.repo.model.RSSEntry;
import org.sagebionetworks.repo.model.RSSFeed;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.RssServiceAsync;
import org.sagebionetworks.web.client.StackConfigServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.HomeView;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.sun.syndication.feed.synd.SyndContent;

@SuppressWarnings("unused")
public class HomePresenter extends AbstractActivity implements HomeView.Presenter {
	public static final String KEY_DATASETS_SELECTED_COLUMNS_COOKIE = "org.sagebionetworks.selected.dataset.columns";
	
	private Home place;
	private HomeView view;
	private GlobalApplicationState globalApplicationState;
	private CookieProvider cookieProvider;
	private AuthenticationController authenticationController;
	private StackConfigServiceAsync stackConfigService;
	private RssServiceAsync rssService;
	private NodeModelCreator nodeModelCreator;
	
	@Inject
	public HomePresenter(HomeView view, 
			CookieProvider cookieProvider, 
			AuthenticationController authenticationController, 
			GlobalApplicationState globalApplicationState,
			StackConfigServiceAsync stackConfigService,
			RssServiceAsync rssService,
			NodeModelCreator nodeModelCreator){
		this.view = view;
		// Set the presenter on the view
		this.cookieProvider = cookieProvider;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.stackConfigService = stackConfigService;
		this.rssService = rssService;
		this.nodeModelCreator = nodeModelCreator;
		this.view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	public void setPlace(Home place) {
		this.place = place;		
		view.setPresenter(this);		
		if(place != null && place.toToken() != null) {
			if(place.toToken().equals(DisplayConstants.TURN_DEMO_ON_TOKEN)) {
				DisplayConstants.showDemoHtml = true;
				authenticationController.saveShowDemo();
			} else if(place.toToken().equals(DisplayConstants.TURN_DEMO_OFF_TOKEN)) {
				DisplayConstants.showDemoHtml = false;
				authenticationController.saveShowDemo();
			}
		} 
		view.refresh();
		
		rssService.getFeedData(DisplayUtils.NEWS_FEED_URL, 4, true, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					view.showNews(getHtml(result));
				} catch (RestServiceException e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showNews("<p>"+DisplayConstants.NEWS_UNAVAILABLE_TEXT+"</p>");
			}
		});
	}
	
	public void loadBccOverviewDescription() {
		rssService.getPageContent(DisplayUtils.BCC_SUMMARY_CONTENT_PAGE_URL, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				view.showBccOverview(result);
			}
			@Override
			public void onFailure(Throwable caught) {
				//don't show the bcc overview
			}
		});
	}
	
	public String getHtml(String rssFeedJson) throws RestServiceException {
		RSSFeed feed = nodeModelCreator.createEntity(rssFeedJson, RSSFeed.class);
		StringBuilder htmlResponse = new StringBuilder();
	
		for (int i = 0; i < feed.getEntries().size(); i++) {
			RSSEntry entry = feed.getEntries().get(i);
			//every 4, set as the last (if we support more than 4)
			String lastString = (i+1)%4==0 ? "last" : "";
			htmlResponse.append("<div class=\"span-6 serv "+lastString+"\"><div class=\"icon-white-big left icon161-white\" style=\"background-color: rgb(122, 122, 122);\"></div><h5 style=\"margin-left: 25px;\"><a href=\"");
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

	@Override
    public String mayStop() {
        view.clear();
        return null;
    }

	@Override
	public boolean showLoggedInDetails() {
		return authenticationController.isLoggedIn();
	}

	@Override
	public void showBCCSignup(AsyncCallback<String> callback) {
		stackConfigService.getBCCSignupEnabled(callback);
	}
	
	
}
