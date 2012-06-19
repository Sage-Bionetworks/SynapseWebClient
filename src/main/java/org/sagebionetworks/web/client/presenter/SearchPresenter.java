package org.sagebionetworks.web.client.presenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwttime.time.DateTime;
import org.sagebionetworks.repo.model.search.Hit;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.KeyValue;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.SearchView;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;
import org.sagebionetworks.web.client.widget.search.PaginationUtil;
import org.sagebionetworks.web.shared.EntityType;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class SearchPresenter extends AbstractActivity implements SearchView.Presenter {
	
	//private final List<String> FACETS_DEFAULT = Arrays.asList(new String[] {"node_type","disease","species","tissue","platform","num_samples","created_by","modified_by","created_on","modified_on","acl","reference"});
	
	private Search place;
	private SearchView view;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private JSONObjectAdapter jsonObjectAdapter;
	private EntityTypeProvider entityTypeProvider;
	private IconsImageBundle iconsImageBundle;
	
	private SearchQuery currentSearch;
	private SearchResults currentResult;
	private boolean newQuery = false;
	private Map<String,String> timeValueToDisplay = new HashMap<String, String>();
	private DateTime searchStartTime;
	
	private Place redirect; 
	
	
	@Inject
	public SearchPresenter(SearchView view,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController,
			SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator,
			JSONObjectAdapter jsonObjectAdapter,
			EntityTypeProvider entityTypeProvider,
			IconsImageBundle iconsImageBundle) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.entityTypeProvider = entityTypeProvider;
		this.iconsImageBundle = iconsImageBundle;
		
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
		redirect = null;
		String queryTerm = place.toToken();

		if (willRedirect(queryTerm)) {
			redirect = new Synapse(queryTerm);
			return;
		}

		currentSearch = checkForJson(place);
		executeSearch();
	}

	private SearchQuery checkForJson(Search place) {
		String queryString = place.toToken();

		SearchQuery query = getBaseSearchQuery();
		query.setQueryTerm(Arrays.asList(queryString.split(" ")));
		
		// if query parses into SearchQuery, use that, otherwise use it as a
		// search Term
		if (queryString != null && queryString.startsWith("{")) {
			try {
				query = new SearchQuery(jsonObjectAdapter.createNew(queryString));
				// passed a searchQuery
			} catch (JSONObjectAdapterException e) {
				// fall through to a use as search term
			}
		} 

		return query;
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
		
		// set to first page
		currentResult.setStart(new Long(0));
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
		return DisplayUtils.FACETS_DISPLAY_ORDER;
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

	@Override
	public List<PaginationEntry> getPaginationEntries(int nPerPage, int nPagesToShow) {
		Long nResults = currentResult.getFound();
		Long start = currentResult.getStart();
		if(nResults == null || start == null)
			return null;
		return PaginationUtil.getPagination(nResults.intValue(), start.intValue(), nPerPage, nPagesToShow);
	}

	public Place getRedirect() {
		return redirect;
	}

	@Override
	public ImageResource getIconForHit(Hit hit) {
		if(hit == null) return null;
		EntityType type = entityTypeProvider.getEntityTypeForString(hit.getNode_type());
		return DisplayUtils.getSynapseIconForEntityType(type, DisplayUtils.IconSize.PX24, iconsImageBundle);
	}

	
	/*
	 * Private Methods
	 */

	private boolean willRedirect(String queryTerm) {
		if(queryTerm.startsWith(DisplayUtils.SYNAPSE_ID_PREFIX)) {
			String remainder = queryTerm.replaceFirst(DisplayUtils.SYNAPSE_ID_PREFIX, "");
			if(remainder.matches("^[0-9]+$")) {
				return true;
			}
		}
		return false;
	}

	private SearchQuery getBaseSearchQuery() {
		SearchQuery query = DisplayUtils.getDefaultSearchQuery();
		timeValueToDisplay.clear();
		searchStartTime = new DateTime();		
		newQuery = true;		
		return query;
	}
	
	private void executeSearch() { 						
		view.showLoading();
		// Is there a search defined? If not, display empty result.
		if (isEmptyQuery()) {
			currentResult = new SearchResults();
			currentResult.setFound(new Long(0));
			view.setSearchResults(currentResult, "", newQuery);
			newQuery = false;
			return;
		}
		
		JSONObjectAdapter adapter = jsonObjectAdapter.createNew();
		try {
			currentSearch.writeToJSONObject(adapter);
			synapseClient.search(adapter.toJSONString(), new AsyncCallback<EntityWrapper>() {			
				@Override
				public void onSuccess(EntityWrapper result) {
					currentResult = new SearchResults();		
					try {
						currentResult = nodeModelCreator.createEntity(result, SearchResults.class);
					} catch (RestServiceException e) {
						onFailure(null);					
					}									
					view.setSearchResults(currentResult, join(currentSearch.getQueryTerm(), " "), newQuery);
					newQuery = false;
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.getLoggedInUser())) {
						view.showErrorMessage(DisplayConstants.ERROR_GENERIC_RELOAD);
					}
				}
			});
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.ERROR_GENERIC);
		}
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
