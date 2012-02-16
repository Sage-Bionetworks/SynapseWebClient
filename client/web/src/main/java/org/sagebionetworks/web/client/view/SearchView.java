package org.sagebionetworks.web.client.view;

import java.util.List;

import org.gwttime.time.DateTime;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.KeyValue;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;

import com.google.gwt.user.client.ui.IsWidget;

public interface SearchView extends IsWidget, SynapseView {
	
	/**
	 * Sets the search results to display
	 * @param searchResults
	 */
	public void setSearchResults(SearchResults searchResults, String searchTerm, boolean newQuery);
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);	
	
	public interface Presenter {		
		
		void setSearchTerm(String queryTerm);
		
		void addFacet(String facetName, String facetValue);
		
		void addTimeFacet(String facetName, String facetValue,
				String displayValue);

		void removeFacet(String facetName, String facetValue);
		
		void clearSearch();
		
		List<KeyValue> getAppliedFacets();
		
		List<String> getFacetDisplayOrder(); 
		
		PlaceChanger getPlaceChanger();

		void setStart(int newStart);

		String getDisplayForTimeFacet(String facetName, String facetValue);
		
		DateTime getSearchStartTime();
		
		List<PaginationEntry> getPaginationEntries(int nPerPage, int nPagesToShow);
		
	}

}
