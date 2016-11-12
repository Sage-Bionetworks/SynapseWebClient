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
	public static final int NUMBER_OF_STEPS = 200;
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
		view.setColumnName(facet.getColumnName());
		Number minMin = parseNumber(facet.getColumnMin());
		Number maxMax = parseNumber(facet.getColumnMax());
		double stepSize = getStepSize(minMin, maxMax);
		
		Number min = parseNumber(facet.getSelectedMin());
		if (min == null) {
			min = minMin;
		}
		Number max = parseNumber(facet.getSelectedMax());
		if (max == null) {
			max = maxMax;
		}
		if (minMin != null) {
			view.setMin(minMin.doubleValue());
		}
		if (maxMax != null) {
			view.setMax(maxMax.doubleValue());
		}
		if (min != null && max != null) {
			view.setRange(new Range(min.doubleValue(), max.doubleValue()));	
		}
		
		view.setSliderStepSize(stepSize);
	}
	
	public double getStepSize(Number min, Number max) {
		double stepSize = 1;
		if (min != null && max != null) {
			stepSize = Math.round((max.doubleValue() - min.doubleValue()) / NUMBER_OF_STEPS);
			if (stepSize < 1) {
				stepSize = 1;
			}
		}
		return stepSize;
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
	public void onFacetChange(Range selectedRange) {
		FacetColumnRangeRequest facetColumnRangeRequest = new FacetColumnRangeRequest();
		facetColumnRangeRequest.setColumnName(facet.getColumnName());
		facetColumnRangeRequest.setMin(Double.toString(selectedRange.getMinValue()));
		facetColumnRangeRequest.setMax(Double.toString(selectedRange.getMaxValue()));
		onFacetRequest.invoke(facetColumnRangeRequest);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
