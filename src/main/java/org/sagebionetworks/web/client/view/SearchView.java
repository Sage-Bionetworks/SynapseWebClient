package org.sagebionetworks.web.client.view;

import java.util.Date;
import java.util.List;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.search.Hit;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.KeyValue;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

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
		
		void setStart(int newStart);

		String getDisplayForTimeFacet(String facetName, String facetValue);
		
		Date getSearchStartTime();
		
		List<PaginationEntry> getPaginationEntries(int nPerPage, int nPagesToShow);

		IconType getIconForHit(Hit hit);
		
		String getCurrentSearchJSON();
		
		Long getStart();
	}

	public void setSynAlertWidget(Widget asWidget);

}
