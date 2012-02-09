package org.sagebionetworks.web.client.presenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwttime.time.DateTime;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.KeyValue;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.SearchView;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class SearchPresenter extends AbstractActivity implements SearchView.Presenter {
	
	//private final List<String> FACETS_DEFAULT = Arrays.asList(new String[] {"node_type","disease","species","tissue","platform","num_samples","created_by","modified_by","created_on","modified_on","acl","reference"});
	private final List<String> FACETS_DISPLAY_ORDER = Arrays.asList(new String[] {"node_type","species","disease","modified_on", "created_on","tissue","num_samples","created_by"});
	
	private Search place;
	private SearchView view;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private JSONObjectAdapter jsonObjectAdapter;
	
	private SearchQuery currentSearch;
	private boolean newQuery = false;
	private Map<String,String> timeValueToDisplay = new HashMap<String, String>();
	private DateTime searchStartTime;
	
	
	@Inject
	public SearchPresenter(SearchView view,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController,
			SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator,
			JSONObjectAdapter jsonObjectAdapter) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.jsonObjectAdapter = jsonObjectAdapter;
		
		currentSearch = getBaseSearchQuery();
		
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	public void setPlace(Search place) {
		this.place = place;
		view.setPresenter(this);
			

		// create initial search query
		String queryString = place.toToken();
		currentSearch = getBaseSearchQuery();
		setSearchTerm(queryString);		
		executeSearch();
	}

	@Override
	public PlaceChanger getPlaceChanger() {
		return globalApplicationState.getPlaceChanger();
	}

	@Override
    public String mayStop() {
        view.clear();
        return null;
    }

	@Override
	public void setSearchTerm(String queryTerm) {		
		if(queryTerm == null) queryTerm = "";
		String oldQueryTerm = join(currentSearch.getQueryTerm(), " ");
		
		// reset search if queryTerm is not prefixed by existing term
		if(currentSearch.getQueryTerm() == null || queryTerm.length() < oldQueryTerm.length()  || !oldQueryTerm.startsWith(queryTerm)) {
			currentSearch = getBaseSearchQuery();					
		}
		
		// set new search term & run search. split each word into its own value
		currentSearch.setQueryTerm(Arrays.asList(queryTerm.split(" ")));					
		executeSearch();
	}

	@Override
	public void addFacet(String facetName, String facetValue) {
		List<KeyValue> bq = currentSearch.getBooleanQuery();
		if(bq == null) {
			bq = new ArrayList<KeyValue>();			
			currentSearch.setBooleanQuery(bq);
		}

		// check if exists
		boolean exists = false;
		for(KeyValue kv : bq) {
			if(kv.getKey().equals(facetName) && kv.getValue().equals(facetValue)) {
				exists = true;
				break;
			}
		}
		
		// only add if not exists already. but do run the search
		if(!exists) {	
					
			// add facet to query list
			KeyValue kv = new KeyValue();		
			kv.setKey(facetName);
			kv.setValue(facetValue);		
			bq.add(kv);
			
		}
		
		executeSearch();
	}

	@Override
	public void addTimeFacet(String facetName, String facetValue, String displayValue) {
		timeValueToDisplay.put(createTimeValueKey(facetName, facetValue), displayValue);
		addFacet(facetName, facetValue);
	}
	
	@Override
	public String getDisplayForTimeFacet(String facetName, String facetValue) {
		return timeValueToDisplay.get(createTimeValueKey(facetName, facetValue));
	}
	
	
	@Override
	public void removeFacet(String facetName, String facetValue) {
		List<KeyValue> bq = currentSearch.getBooleanQuery();
		// check for existing facet and remove it
		for(KeyValue kv : bq) {
			if(kv.getKey().equals(facetName) && kv.getValue().equals(facetValue)) {
				bq.remove(kv);
				break;
			}
		}
		
		executeSearch();
	}

	@Override
	public void clearSearch() {
		currentSearch = getBaseSearchQuery();
		executeSearch();
	}
	
	@Override
	public List<KeyValue> getAppliedFacets() {
		List<KeyValue> bq = currentSearch.getBooleanQuery(); 
		if(bq == null) {
			return new ArrayList<KeyValue>();
		} else {
			return bq;
		}
	}

	@Override
	public List<String> getFacetDisplayOrder() {
		return FACETS_DISPLAY_ORDER;
	}

	@Override
	public void setStart(int newStart) {
		currentSearch.setStart(new Long(newStart));
		executeSearch();
	}

	@Override
	public DateTime getSearchStartTime() {
		if(searchStartTime == null) searchStartTime = new DateTime();
		return searchStartTime;		
	}

	
	/*
	 * Private Methods
	 */
	private SearchQuery getBaseSearchQuery() {		
		SearchQuery query = new SearchQuery();
		// start with a blank, valid query
		query.setQueryTerm(Arrays.asList(new String[] {""}));		
		query.setReturnFields(Arrays.asList(new String[] {"name","description","id"}));		
		query.setFacet(FACETS_DISPLAY_ORDER);
		
		timeValueToDisplay.clear();
		searchStartTime = new DateTime();
		
		newQuery = true;
		return query;
	}

	private void executeSearch() {
		view.showLoading();
		JSONObjectAdapter adapter = jsonObjectAdapter.createNew();
		try {
			currentSearch.writeToJSONObject(adapter);
			synapseClient.search(adapter.toJSONString(), new AsyncCallback<EntityWrapper>() {			
				@Override
				public void onSuccess(EntityWrapper result) {
					SearchResults results = new SearchResults();		
					try {
						results = nodeModelCreator.createSearchResults(result);
					} catch (RestServiceException e) {
						if(!DisplayUtils.handleServiceException(e, globalApplicationState.getPlaceChanger(), authenticationController.getLoggedInUser())) {					
							onFailure(null);					
						} 						
					}					
					view.setSearchResults(results, join(currentSearch.getQueryTerm(), " "), newQuery);
					newQuery = false;
				}
				
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(DisplayConstants.ERROR_GENERIC_RELOAD);
				}
			});
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.ERROR_GENERIC);
		}
	}


	private static String join(List<String> list, String delimiter) {
		StringBuilder sb = new StringBuilder();
		for (String item : list) {
			sb.append(item);
			sb.append(delimiter);
		}
		String str = sb.toString();
		if (str.length() > 0) {
			str = str.substring(0, str.length()-1);
		}
		return str;
	}

	private String createTimeValueKey(String facetName, String facetValue) {
		return facetName + facetValue;
	}

}
