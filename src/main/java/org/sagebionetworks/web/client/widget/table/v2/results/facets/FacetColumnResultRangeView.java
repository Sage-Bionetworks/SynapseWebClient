package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import com.google.gwt.user.client.ui.IsWidget;

public interface FacetColumnResultRangeView extends IsWidget {
	public interface Presenter {
		void onFacetChange();
	}
	void setPresenter(Presenter p);
	void setMin(String min);
	String getMin();
	void setMax(String max);
	String getMax();
}
