package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface FacetColumnResultRangeView extends IsWidget {
	public interface Presenter {
		void onFacetChange();
	}
	void setPresenter(Presenter p);
	String getMin();
	String getMax();
	void setSynAlert(Widget w);
}
