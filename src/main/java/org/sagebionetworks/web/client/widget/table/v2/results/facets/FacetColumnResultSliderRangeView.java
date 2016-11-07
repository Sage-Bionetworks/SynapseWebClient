package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import org.gwtbootstrap3.extras.slider.client.ui.Range;

import com.google.gwt.user.client.ui.IsWidget;

public interface FacetColumnResultSliderRangeView extends IsWidget {
	public interface Presenter {
		void onFacetChange(Range newRange);
	}
	void setMin(double min);
	void setMax(double max);
	void setRange(Range range);
	void setPresenter(Presenter p);
	void setColumnName(String columnName);
	void setSliderStepSize(double step);
}
