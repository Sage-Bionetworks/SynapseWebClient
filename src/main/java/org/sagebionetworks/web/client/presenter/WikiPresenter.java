package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.RssServiceAsync;
import org.sagebionetworks.web.client.place.WikiPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.WikiView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class WikiPresenter extends AbstractActivity implements WikiView.Presenter {
		
	private WikiPlace place;
	private WikiView view;
	private AuthenticationController authenticationController;
	private NodeModelCreator nodeModelCreator;
	private RssServiceAsync rssService;
	private GlobalApplicationState globalApplicationState;
	
	@Inject
	public WikiPresenter(WikiView view, 
			AuthenticationController authenticationController, 
			GlobalApplicationState globalApplicationState, 
			NodeModelCreator nodeModelCreator, 
			RssServiceAsync rssService){
		this.authenticationController = authenticationController;
		this.view = view;
		this.nodeModelCreator = nodeModelCreator;
		this.rssService = rssService;
		this.globalApplicationState = globalApplicationState;
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	public void setPlace(WikiPlace place) {
		this.place = place;
		this.view.setPresenter(this);
		
		String cacheProviderId = place.toToken();
		//the token is the source page cached content provider id to pull from
		loadSourceContent(cacheProviderId);
	}
	
	@Override
	public void loadSourceContent(String cacheProviderId) {
		rssService.getCachedContent(cacheProviderId, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				view.showPage(DisplayUtils.fixWikiLinks(DisplayUtils.fixEmbeddedYouTube(result)));
			}
			@Override
			public void onFailure(Throwable caught) {
				DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.getLoggedInUser());
			}
		});
		
	}
	
	@Override
    public String mayStop() {
        view.clear();
        return null;
    }
}
