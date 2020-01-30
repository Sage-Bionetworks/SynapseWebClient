package org.sagebionetworks.web.client.view;

import java.util.Date;
import java.util.List;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.search.Hit;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.KeyRange;
import org.sagebionetworks.repo.model.search.query.KeyValue;
import org.sagebionetworks.web.client.SynapseView;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface SearchView extends IsWidget, SynapseView {

	/**
	 * Sets the search results to display
	 * 
	 * @param searchResults
	 */
	public void setSearchResults(SearchResults searchResults, String searchTerm);

	/**
	 * Get widget representing search result hits
	 * 
	 * @param searchResults
	 * @return
	 */
	Widget getResults(SearchResults searchResults, String searchTerm, boolean isFirstPage);

	/**
	 * Set this view's presenter
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public interface Presenter {

		void setSearchTerm(String queryTerm);

		void addFacet(String facetName, String facetValue);

		void removeTimeFacetAndRefresh(String facetName);

		void addTimeFacet(String facetName, String facetValue, String displayValue);

		void removeFacet(String facetName, String facetValue);

		void clearSearch();

		List<KeyValue> getAppliedFacets();

		List<KeyRange> getAppliedTimeFacets();

		List<String> getFacetDisplayOrder();

		String getDisplayForTimeFacet(String facetName, String facetValue);

		Date getSearchStartTime();

		IconType getIconForHit(Hit hit);

		String getCurrentSearchJSON();

		Long getStart();
	}

	public void setSynAlertWidget(Widget asWidget);

	public void setLoadingMoreContainerWidget(Widget w);

}
