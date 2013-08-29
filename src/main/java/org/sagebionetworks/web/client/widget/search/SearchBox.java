package org.sagebionetworks.web.client.widget.search;

import java.util.Arrays;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.SearchQueryUtils;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SearchBox implements SearchBoxView.Presenter, SynapseWidgetPresenter {
	
	private SearchBoxView view;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private HandlerManager handlerManager = new HandlerManager(this);
	private Entity entity;
	private EntityTypeProvider entityTypeProvider;
	private AdapterFactory adapterFactory;
	private SynapseClientAsync synapseClient;
	private boolean searchAll = false;
	
	@Inject
	public SearchBox(SearchBoxView view, 
			NodeModelCreator nodeModelCreator,
			AuthenticationController authenticationController,
			EntityTypeProvider entityTypeProvider,
			GlobalApplicationState globalApplicationState,
			AdapterFactory adapterFactory,
			SynapseClientAsync synapseClient) {
		this.view = view;
		this.nodeModelCreator = nodeModelCreator;
		this.authenticationController = authenticationController;
		this.entityTypeProvider = entityTypeProvider;
		this.globalApplicationState = globalApplicationState;
		this.adapterFactory = adapterFactory;
		this.synapseClient = synapseClient;
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
		if(searchAll) {
			SearchQuery query = SearchQueryUtils.getAllTypesSearchQuery();
			query.setQueryTerm(Arrays.asList(value.split(" ")));
			try {
				value = query.writeToJSONObject(adapterFactory.createNew()).toJSONString();
			} catch (JSONObjectAdapterException e) {
				// if fail, fall back on regular search
			}
		}
		DisplayUtils.searchForTerm(value, globalApplicationState, synapseClient);
	}

	@Override
	public void setSearchAll(boolean searchAll) {
		this.searchAll = searchAll;
	}

	public void setVisible(boolean isVisible) {
		view.setVisible(isVisible);
	}

	
	/*
	 * Private Methods
	 */
}
