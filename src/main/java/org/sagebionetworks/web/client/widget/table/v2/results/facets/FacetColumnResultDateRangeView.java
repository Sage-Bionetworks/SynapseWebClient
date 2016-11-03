package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import java.util.Date;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface FacetColumnResultDateRangeView extends IsWidget {
	public interface Presenter {
		void onFacetChange();
	}
	void setPresenter(Presenter p);
	void setMin(Date min);
	void setMax(Date max);
	Date getMin();
	Date getMax();
	void setSynAlert(Widget w);
}
