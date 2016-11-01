package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import org.gwtbootstrap3.extras.slider.client.ui.Range;
import org.sagebionetworks.repo.model.table.FacetColumnRangeRequest;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.repo.model.table.FacetColumnResultRange;
import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FacetColumnResultSliderRangeWidget implements IsWidget, FacetColumnResultSliderRangeView.Presenter {
	FacetColumnResultSliderRangeView view;
	FacetColumnResultRange facet;
	CallbackP<FacetColumnRequest> onFacetRequest;
	
	@Inject
	public FacetColumnResultSliderRangeWidget(FacetColumnResultSliderRangeView view) {
		this.view = view;
		view.setPresenter(this);
	}
	
	public void configure(FacetColumnResultRange facet, CallbackP<FacetColumnRequest> onFacetRequest) {
		this.facet = facet;
		this.onFacetRequest = onFacetRequest;
		
		Number minMin = parseNumber(facet.getColumnMin());
		Number maxMax = parseNumber(facet.getColumnMax());
		double stepSize = 1;
		if (minMin != null && maxMax != null) {
			stepSize = Math.round((maxMax.doubleValue() - minMin.doubleValue()) / 200);
		}
		view.setSliderMin(minMin.doubleValue());
		view.setSliderMax(maxMax.doubleValue());
		
		Number min = parseNumber(facet.getSelectedMin());
		if (min == null) {
			min = minMin;
		}
		Number max = parseNumber(facet.getSelectedMax());
		if (max == null) {
			max = maxMax;
		}
		view.setSliderRange(new Range(min.doubleValue(), max.doubleValue()));
		view.setSliderStepSize(stepSize);
	}
	
	public static Number parseNumber(String s) {
		Number number = null;
		if (s != null) {
			try {
		        number = Double.parseDouble(s);
		    } catch(NumberFormatException e) {
		        number = Long.parseLong(s);
		    }
		}
	    return number;
	}
	
	@Override
	public void onFacetChange() {
		FacetColumnRangeRequest facetColumnRangeRequest = new FacetColumnRangeRequest();
		Range selectedRange = view.getSliderRange();
		facetColumnRangeRequest.setMin(Double.toString(selectedRange.getMinValue()));
		facetColumnRangeRequest.setMax(Double.toString(selectedRange.getMaxValue()));
		onFacetRequest.invoke(facetColumnRangeRequest);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
