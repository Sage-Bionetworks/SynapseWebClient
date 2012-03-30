package org.sagebionetworks.web.client.widget.search;

import java.util.ArrayList;

import org.sagebionetworks.repo.model.Code;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.search.query.KeyValue;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.services.NodeServiceAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.EntityType;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HomeSearchBox implements HomeSearchBoxView.Presenter, SynapseWidgetPresenter {
	
	private HomeSearchBoxView view;
	private PlaceChanger placeChanger;
	private NodeServiceAsync nodeService;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private HandlerManager handlerManager = new HandlerManager(this);
	private Entity entity;
	private EntityTypeProvider entityTypeProvider;
	private JSONObjectAdapter jsonObjectAdapter;
	
	@Inject
	public HomeSearchBox(HomeSearchBoxView view, NodeServiceAsync nodeService,
			NodeModelCreator nodeModelCreator,
			AuthenticationController authenticationController,
			EntityTypeProvider entityTypeProvider,
			GlobalApplicationState globalApplicationState,
			JSONObjectAdapter jsonObjectAdapter) {
		this.view = view;
		this.nodeService = nodeService;
		this.nodeModelCreator = nodeModelCreator;
		this.authenticationController = authenticationController;
		this.entityTypeProvider = entityTypeProvider;
		this.globalApplicationState = globalApplicationState;
		this.jsonObjectAdapter = jsonObjectAdapter;
		
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
	public PlaceChanger getPlaceChanger() {
		return globalApplicationState.getPlaceChanger();
	}

	@Override
	public void setPlaceChanger(PlaceChanger placeChanger) {
	}

	@Override
	public void search(String value) {		
		globalApplicationState.getPlaceChanger().goTo(new Search(value));
	}

	@Override
	public void searchAllProjects() {		
		search(getSearchQueryForType("project"));	
	}

	@Override
	public void searchAllData() {
		search(getSearchQueryForType("data"));
	}

	@Override
	public void searchAllStudies() {
		search(getSearchQueryForType("study"));
	}

	@Override
	public void searchAllCode() {
		search(getSearchQueryForType("code"));
	}
	
	/*
	 * Private Methods
	 */
	private String getSearchQueryForType(String entityType) {
		String json = null;
		SearchQuery query = DisplayUtils.getDefaultSearchQuery();
		ArrayList<KeyValue> bq = new ArrayList<KeyValue>();
		KeyValue kv = new KeyValue();
		kv.setKey(DisplayUtils.SEARCH_KEY_NODE_TYPE);				
		kv.setValue(entityType); 
		bq.add(kv);
		query.setBooleanQuery(bq);
		try {
			json = query.writeToJSONObject(jsonObjectAdapter.createNew()).toJSONString();
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.ERROR_GENERIC);
		}
		return json;
	}

}
