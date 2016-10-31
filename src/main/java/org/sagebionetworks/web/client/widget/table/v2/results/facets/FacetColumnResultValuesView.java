package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import com.google.gwt.user.client.ui.IsWidget;

public interface FacetColumnResultValuesView extends IsWidget {
	public interface Presenter {
		void onFacetChange(String facetValue);
	}
	void setPresenter(Presenter p);
	void setColumnName(String columnName);
	void clearValues();
	void addValue(boolean isSelected, String facetValue, Long count);
}
