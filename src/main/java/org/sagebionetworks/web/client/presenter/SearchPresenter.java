package org.sagebionetworks.web.client.presenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.EntityTypeUtils;
import org.sagebionetworks.repo.model.search.Hit;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.KeyValue;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.SearchView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;
import org.sagebionetworks.web.client.widget.search.PaginationUtil;
import org.sagebionetworks.web.shared.SearchQueryUtils;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class SearchPresenter extends AbstractActivity implements SearchView.Presenter, Presenter<Search> {
	
	//private final List<String> FACETS_DEFAULT = Arrays.asList(new String[] {"node_type","disease","species","tissue","platform","num_samples","created_by","modified_by","created_on","modified_on","acl","reference"});
	
	private Search place;
	private SearchView view;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private SynapseClientAsync synapseClient;
	private JSONObjectAdapter jsonObjectAdapter;
	private IconsImageBundle iconsImageBundle;
	private SynapseAlert synAlert;
	
	private SearchQuery currentSearch;
	private SearchResults currentResult;
	private boolean newQuery = false;
	private Map<String,String> timeValueToDisplay = new HashMap<String, String>();
	private Date searchStartTime;
	
	
	@Inject
	public SearchPresenter(SearchView view,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController,
			SynapseClientAsync synapseClient,
			JSONObjectAdapter jsonObjectAdapter,
			IconsImageBundle iconsImageBundle,
			SynapseAlert synAlert) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.synapseClient = synapseClient;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.iconsImageBundle = iconsImageBundle;
		this.synAlert = synAlert;
		currentSearch = getBaseSearchQuery();
		view.setPresenter(this);
		view.setSynAlertWidget(synAlert.asWidget());
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(Search place) {
		this.place = place;
		view.setPresenter(this);
		String queryTerm = place.getSearchTerm();
		if (queryTerm == null) queryTerm = "";

		currentSearch = checkForJson(queryTerm);
		if (place.getStart() != null)
			currentSearch.setStart(place.getStart());
		executeSearch();
	}

	@Override
    public String mayStop() {
        view.clear();
        return null;
    }

	@Override
	public void setSearchTerm(String queryTerm) {		
		globalApplicationState.getPlaceChanger().goTo(new Search(queryTerm));
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
			
			// set start back to zero so we go to first page with the new facet
			currentSearch.setStart(new Long(0));			
		}

		executeNewSearch();
	}

	private void executeNewSearch() {
		JSONObjectAdapter adapter = jsonObjectAdapter.createNew();
		
		try {
			currentSearch.writeToJSONObject(adapter);
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.ERROR_GENERIC);
		}
		setSearchTerm(adapter.toJSONString());
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
		
		// set to first page
		currentResult.setStart(new Long(0));
		executeNewSearch();
	}

	@Override
	public void clearSearch() {
		currentSearch = getBaseSearchQuery();
		executeNewSearch();
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
		return SearchQueryUtils.FACETS_DISPLAY_ORDER;
	}

	@Override
	public void setStart(int newStart) {
		currentSearch.setStart(new Long(newStart));
		executeNewSearch();
	}
	
	@Override
	public Long getStart() {
		return currentSearch.getStart();
	}

	@Override
	public Date getSearchStartTime() {
		if(searchStartTime == null) searchStartTime = new Date();
		return searchStartTime;		
	}

	@Override
	public List<PaginationEntry> getPaginationEntries(int nPerPage, int nPagesToShow) {
		if(currentResult == null) return null;		
		Long nResults = currentResult.getFound();
		Long start = currentResult.getStart();
		if(nResults == null || start == null)
			return null;
		return PaginationUtil.getPagination(nResults.intValue(), start.intValue(), nPerPage, nPagesToShow);		
	}

	@Override
	public IconType getIconForHit(Hit hit) {
		if(hit == null) return null;
		EntityType type = EntityType.valueOf(hit.getNode_type());
		return DisplayUtils.getIconTypeForEntityClassName(EntityTypeUtils.getEntityTypeClassName(type));
	}
	
	@Override
	public String getCurrentSearchJSON() {
		String searchJSON = "";
		JSONObjectAdapter adapter = jsonObjectAdapter.createNew();
		try {
			currentSearch.writeToJSONObject(adapter);
			searchJSON = adapter.toJSONString();
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.ERROR_GENERIC);
		}
		return searchJSON;
	}

	private SearchQuery checkForJson(String queryString) {
		SearchQuery query = getBaseSearchQuery();

		query.setQueryTerm(Arrays.asList(queryString.split(" ")));

		// if query parses into SearchQuery, use that, otherwise use it as a
		// search Term
		if (queryString != null) {
			String fixedQueryString = queryString;
			//check for url encoded
			if (queryString.startsWith("%7B")) {
				fixedQueryString = URL.decode(queryString);
			}
			if (fixedQueryString.startsWith("{")) {
				try {
					query = new SearchQuery(jsonObjectAdapter.createNew(fixedQueryString));
					// passed a searchQuery
				} catch (JSONObjectAdapterException e) {
					// fall through to a use as search term
				}
			}
		} 

		return query;
	}

	private SearchQuery getBaseSearchQuery() {
		SearchQuery query = SearchQueryUtils.getDefaultSearchQuery();
		timeValueToDisplay.clear();
		searchStartTime = new Date();		
		newQuery = true;		
		return query;
	}
	
	private void executeSearch() { 	
		synAlert.clear();
		view.showLoading();
		// Is there a search defined? If not, display empty result.
		if (isEmptyQuery()) {
			currentResult = new SearchResults();
			currentResult.setFound(new Long(0));
			view.setSearchResults(currentResult, "", newQuery);
			newQuery = false;
			return;
		}
		AsyncCallback<SearchResults> callback = new AsyncCallback<SearchResults>() {			
			@Override
			public void onSuccess(SearchResults result) {
				currentResult = result;
				view.setSearchResults(currentResult, join(currentSearch.getQueryTerm(), " "), newQuery);
				newQuery = false;
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.clear();
				synAlert.handleException(caught);
			}
		};
		synapseClient.search(currentSearch, callback);
	}

	private boolean isEmptyQuery() {
		return (currentSearch.getQueryTerm() == null || currentSearch.getQueryTerm().size() == 0 
				|| (currentSearch.getQueryTerm().size() == 1 && "".equals(currentSearch.getQueryTerm().get(0))))
				&& (currentSearch.getBooleanQuery() == null || currentSearch.getBooleanQuery().size() == 0);
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
