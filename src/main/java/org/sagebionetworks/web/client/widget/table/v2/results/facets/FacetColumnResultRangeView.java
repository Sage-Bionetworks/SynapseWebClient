package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import org.gwtbootstrap3.extras.slider.client.ui.Range;

import com.google.gwt.user.client.ui.IsWidget;

public interface FacetColumnResultRangeView extends IsWidget {
	public interface Presenter {
		void onFacetChange();
	}
	void setPresenter(Presenter p);
	void setSliderMin(double min);
	void setSliderMax(double max);
	Range getSliderRange();
	void setSliderRange(Range range);
	void setSliderStepSize(double step);
}
