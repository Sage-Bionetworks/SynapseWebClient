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
import org.sagebionetworks.repo.model.search.query.KeyRange;
import org.sagebionetworks.repo.model.search.query.KeyValue;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.SearchView;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.SearchQueryUtils;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class SearchPresenter extends AbstractActivity implements SearchView.Presenter, Presenter<Search> {
	private SearchView view;
	private GlobalApplicationState globalApplicationState;
	private JSONObjectAdapter jsonObjectAdapter;
	private SynapseAlert synAlert;
	
	private SearchQuery currentSearch;
	private SearchResults currentResult;
	private Map<String,String> timeValueToDisplay = new HashMap<String, String>();
	private Date searchStartTime;
	private SynapseJavascriptClient jsClient;
	
	private LoadMoreWidgetContainer loadMoreWidgetContainer;
	
	@Inject
	public SearchPresenter(SearchView view,
			GlobalApplicationState globalApplicationState,
			SynapseJavascriptClient jsClient,
			JSONObjectAdapter jsonObjectAdapter,
			SynapseAlert synAlert,
			LoadMoreWidgetContainer loadMoreWidgetContainer) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.synAlert = synAlert;
		this.loadMoreWidgetContainer = loadMoreWidgetContainer;
		this.jsClient = jsClient;
		currentSearch = getBaseSearchQuery();
		view.setPresenter(this);
		view.setSynAlertWidget(synAlert.asWidget());
		loadMoreWidgetContainer.configure(new Callback() {
			@Override
			public void invoke() {
				executeSearch();
			}
		});
		view.setLoadingMoreContainerWidget(loadMoreWidgetContainer.asWidget());
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(Search place) {
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
        loadMoreWidgetContainer.clear();
        return null;
    }

	@Override
	public void setSearchTerm(String queryTerm) {
		SearchUtil.searchForTerm(queryTerm, globalApplicationState);
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

		executeNewSearch();
	}

	private void executeNewSearch() {
		currentSearch.setStart(0L);
		view.clear();
		loadMoreWidgetContainer.clear();
		Search searchPlace = new Search(getCurrentSearchJSON());
		globalApplicationState.pushCurrentPlace(searchPlace);
		executeSearch();
	}

	@Override
	public void addTimeFacet(String facetName, String facetValue, String displayValue) {
		timeValueToDisplay.put(createTimeValueKey(facetName, facetValue), displayValue);
		List<KeyRange> rq = currentSearch.getRangeQuery();
		if (rq == null) {
			rq = new ArrayList<>();
			currentSearch.setRangeQuery(rq);
		}
		KeyRange targetKeyRange = null;
		for (KeyRange keyRange : rq) {
			if (facetName.equals(keyRange.getKey())) {
				targetKeyRange = keyRange;
				if (keyRange.getMin().equals(facetValue)) {
					// no change, return
					return;
				}
				break;
			}
		}
		if (targetKeyRange == null) {
			targetKeyRange = new KeyRange();
			targetKeyRange.setKey(facetName);
			rq.add(targetKeyRange);
		}
		targetKeyRange.setMin(facetValue);
		executeNewSearch();
	}
	
	@Override
	public void removeTimeFacetAndRefresh(String facetName) {
		List<KeyRange> rq = currentSearch.getRangeQuery();
		if(rq != null) {
			List<KeyRange> newRq = new ArrayList<KeyRange>();
			for(KeyRange kv : rq) {
				if(!kv.getKey().equals(facetName)) {
					newRq.add(kv);
				}
			}
			currentSearch.setRangeQuery(newRq);
		}

		executeNewSearch();
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
	public List<KeyRange> getAppliedTimeFacets() {
		List<KeyRange> bq = currentSearch.getRangeQuery(); 
		if(bq == null) {
			return new ArrayList<KeyRange>();
		} else {
			return bq;
		}
	}


	@Override
	public List<String> getFacetDisplayOrder() {
		return SearchQueryUtils.FACETS_DISPLAY_ORDER;
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
	public IconType getIconForHit(Hit hit) {
		if(hit == null) return null;
		EntityType type = EntityType.valueOf(hit.getNode_type());
		return org.sagebionetworks.web.client.EntityTypeUtils.getIconTypeForEntityClassName(EntityTypeUtils.getEntityTypeClassName(type));
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
		return query;
	}
	
	private void executeSearch() { 	
		synAlert.clear();
		// Is there a search defined? If not, display empty result.
		if (isEmptyQuery()) {
			currentResult = new SearchResults();
			currentResult.setFound(new Long(0));
			view.setSearchResults(currentResult, "");
			loadMoreWidgetContainer.setIsMore(false);
			return;
		}
		AsyncCallback<SearchResults> callback = new AsyncCallback<SearchResults>() {			
			@Override
			public void onSuccess(SearchResults result) {
				currentResult = result;
				String searchTerm = join(currentSearch.getQueryTerm(), " ");
				boolean isFirstPage = currentSearch.getStart() == null || currentSearch.getStart() == 0L;
				if (isFirstPage) {
					view.setSearchResults(currentResult, searchTerm);
				}
				Long limit = currentSearch.getSize() == null ? 10L : currentSearch.getSize();
				currentSearch.setStart(currentResult.getStart() + limit);
				loadMoreWidgetContainer.add(view.getResults(currentResult, searchTerm, isFirstPage));
				List<Hit> hits = currentResult.getHits();
				boolean isMore = limit.equals(new Long(hits.size()));
				loadMoreWidgetContainer.setIsMore(isMore);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.clear();
				loadMoreWidgetContainer.setIsMore(false);
				synAlert.handleException(caught);
			}
		};
		loadMoreWidgetContainer.setIsProcessing(true);
		jsClient.getSearchResults(currentSearch, callback);
	}

	private boolean isEmptyQuery() {
		return (currentSearch.getQueryTerm() == null || currentSearch.getQueryTerm().size() == 0
				|| (currentSearch.getQueryTerm().size() == 1 && "".equals(currentSearch.getQueryTerm().get(0))))
				&& (currentSearch.getBooleanQuery() == null || currentSearch.getBooleanQuery().size() == 0)
				&& (currentSearch.getRangeQuery() == null || currentSearch.getRangeQuery().size() == 0);
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
