package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.PeopleSearch;
import org.sagebionetworks.web.client.place.TeamSearch;
import org.sagebionetworks.web.client.place.Trash;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.PeopleSearchView;
import org.sagebionetworks.web.client.view.TeamSearchView;
import org.sagebionetworks.web.client.view.TrashView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class PeopleSearchPresenter extends AbstractActivity implements PeopleSearchView.Presenter, Presenter<PeopleSearch> {
	
	private PeopleSearch place;
	private PeopleSearchView view;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private CookieProvider cookieProvider;
	
	@Inject
	public PeopleSearchPresenter(PeopleSearchView view,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator,
			CookieProvider cookieProvider) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		
		view.setPresenter(this);
	}
	
	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(PeopleSearch place) {
		this.place = place;
		this.view.setPresenter(this);
		this.view.clear();
		showView(place);
	}
	
	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	@Override
	public void search(String searchTerm, Integer offset) {
		// TODO Auto-generated method stub
		
	}
	
	
	/*
	 * Private Methods
	 */
	
	private void showView(PeopleSearch place) {
		String searchTerm = place.getSearchTerm();
		Integer offset = place.getStart();
		search(searchTerm, offset);
	}

}
