package org.sagebionetworks.web.client.widget.search;

import java.util.ArrayList;
import java.util.Arrays;

import org.sagebionetworks.repo.model.search.query.KeyValue;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.PeopleSearch;
import org.sagebionetworks.web.client.presenter.SearchUtil;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.SearchQueryUtils;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HomeSearchBox implements HomeSearchBoxView.Presenter, SynapseWidgetPresenter {
	
	private HomeSearchBoxView view;
	private GlobalApplicationState globalApplicationState;
	private JSONObjectAdapter jsonObjectAdapter;
	private SynapseClientAsync synapseClient;
	private boolean searchAll = false;
	
	@Inject
	public HomeSearchBox(HomeSearchBoxView view, 
			GlobalApplicationState globalApplicationState,
			JSONObjectAdapter jsonObjectAdapter, SynapseClientAsync synapseClient) {
		this.view = view;		
		this.globalApplicationState = globalApplicationState;
		this.jsonObjectAdapter = jsonObjectAdapter;
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
		if (value != null && !value.isEmpty()) {
			if (value.charAt(0) == '@') {
				globalApplicationState.getPlaceChanger().goTo(new PeopleSearch(value.substring(1)));
			} else {
				if(searchAll) {
					SearchQuery query = SearchQueryUtils.getAllTypesSearchQuery();
					query.setQueryTerm(Arrays.asList(value.split(" ")));
					try {
						value = query.writeToJSONObject(jsonObjectAdapter.createNew()).toJSONString();
					} catch (JSONObjectAdapterException e) {
						// if fail, fall back on regular search
					}
				}
				SearchUtil.searchForTerm(value, globalApplicationState, synapseClient);
			}
		}
	}
	
	@Override
	public String getSearchAllProjectsLink() {		
		return DisplayUtils.getSearchHistoryToken(getSearchQueryForType("project"));	
	}

	@Override
	public String getSearchAllDataLink() {
		return DisplayUtils.getSearchHistoryToken(getSearchQueryForType("data"));
	}

	@Override
	public String getSearchAllStudiesLink() {
		return DisplayUtils.getSearchHistoryToken(getSearchQueryForType("study"));
	}

	@Override
	public String getSearchAllCodeLink() {
		return DisplayUtils.getSearchHistoryToken(getSearchQueryForType("code"));
	}
	
	@Override
	public void setSearchAll(boolean searchAll) {
		this.searchAll = searchAll;
	}
	
	/*
	 * Private Methods
	 */
	private String getSearchQueryForType(String entityType) {
		String json = null;
		SearchQuery query = SearchQueryUtils.getDefaultSearchQuery();
		ArrayList<KeyValue> bq = new ArrayList<KeyValue>();
		KeyValue kv = new KeyValue();
		kv.setKey(SearchQueryUtils.SEARCH_KEY_NODE_TYPE);				
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
