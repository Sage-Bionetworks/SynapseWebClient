package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import org.gwtbootstrap3.extras.slider.client.ui.Range;
import org.sagebionetworks.repo.model.table.FacetColumnRangeRequest;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.repo.model.table.FacetColumnResultRange;
import org.sagebionetworks.repo.model.table.TableConstants;
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
		Number minMin = Double.parseDouble(facet.getColumnMin());
		Number maxMax = Double.parseDouble(facet.getColumnMax());
		view.setMin(minMin.doubleValue());
		view.setMax(maxMax.doubleValue());
		double stepSize = getStepSize(minMin, maxMax);
		view.setSliderStepSize(stepSize);
		
		
		boolean isAny = facet.getSelectedMin() == null && facet.getSelectedMax() == null;
		boolean isNotSetFilter = (facet.getSelectedMin() != null && facet.getSelectedMin().equals(TableConstants.NULL_VALUE_KEYWORD)) ||
				(facet.getSelectedMax() != null && facet.getSelectedMax().equals(TableConstants.NULL_VALUE_KEYWORD));
		if (isAny) {
			view.setIsAnyValue();
		} else if (isNotSetFilter) {
			view.setIsNotSet();
		} else {
			view.setIsRange();
		}

		Number min = minMin;
		if (facet.getSelectedMin() != null) {
			min = Double.parseDouble(facet.getSelectedMin());
		}
		Number max = maxMax;
		if (facet.getSelectedMax() != null) {
			max = Double.parseDouble(facet.getSelectedMax());
		}
		if (min != null && max != null) {
			view.setRange(new Range(min.doubleValue(), max.doubleValue()));	
		}
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
	
	@Override
	public void onFacetChange() {
		FacetColumnRangeRequest facetColumnRangeRequest = new FacetColumnRangeRequest();
		facetColumnRangeRequest.setColumnName(facet.getColumnName());
		
		if (view.isNotSet()) {
			facetColumnRangeRequest.setMin(TableConstants.NULL_VALUE_KEYWORD);
			facetColumnRangeRequest.setMax(TableConstants.NULL_VALUE_KEYWORD);
		} else if (!view.isAnyValue()) {
			Range selectedRange = view.getRange();
			facetColumnRangeRequest.setMin(Double.toString(selectedRange.getMinValue()));
			facetColumnRangeRequest.setMax(Double.toString(selectedRange.getMaxValue()));
		}
		// note, if isAnyValue, request contains null min and null max
		onFacetRequest.invoke(facetColumnRangeRequest);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
