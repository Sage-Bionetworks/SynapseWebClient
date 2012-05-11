package org.sagebionetworks.web.client.widget.search;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.services.NodeServiceAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SearchBox implements SearchBoxView.Presenter, SynapseWidgetPresenter {
	
	private SearchBoxView view;
	private NodeServiceAsync nodeService;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private HandlerManager handlerManager = new HandlerManager(this);
	private Entity entity;
	private EntityTypeProvider entityTypeProvider;
	
	@Inject
	public SearchBox(SearchBoxView view, NodeServiceAsync nodeService, NodeModelCreator nodeModelCreator, AuthenticationController authenticationController, EntityTypeProvider entityTypeProvider, GlobalApplicationState globalApplicationState) {
		this.view = view;
		this.nodeService = nodeService;
		this.nodeModelCreator = nodeModelCreator;
		this.authenticationController = authenticationController;
		this.entityTypeProvider = entityTypeProvider;
		this.globalApplicationState = globalApplicationState;
		
		view.setPresenter(this);
	}	
	
	@Override
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();		
	}

	@SuppressWarnings("unchecked")
	public void clearState() {
		view.clear();
	}

	@Override
	public void search(String value) {		
		globalApplicationState.getPlaceChanger().goTo(new Search(value));
	}

	
	/*
	 * Private Methods
	 */
}
