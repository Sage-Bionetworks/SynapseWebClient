package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import org.gwtbootstrap3.client.ui.html.Span;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface FacetColumnResultValuesView extends IsWidget {
	public interface Presenter {
		void onFacetChange(String facetValue);
	}

	void setPresenter(Presenter p);

	void setColumnName(String columnName);

	void clearValues();

	void addValue(boolean isSelected, Widget displayWidget, Long count, String originalFacetValue);

	void addValueToOverflow(boolean isSelected, Widget displayWidget, Long count, String originalFacetValue);

	void setShowAllButtonVisible(boolean visible);

	void setShowAllButtonText(String text);

	Span getSpanWithText(String text);
}
