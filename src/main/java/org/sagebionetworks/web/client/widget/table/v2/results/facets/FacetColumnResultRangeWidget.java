package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import org.gwtbootstrap3.extras.slider.client.ui.Range;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.FacetColumnRangeRequest;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.repo.model.table.FacetColumnResultRange;
import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FacetColumnResultRangeWidget implements IsWidget, FacetColumnResultRangeView.Presenter {
	FacetColumnResultRangeView view;
	FacetColumnResultRange facet;
	CallbackP<FacetColumnRequest> onFacetRequest;
	ColumnModel columnModel;
	
	@Inject
	public FacetColumnResultRangeWidget(FacetColumnResultRangeView view) {
		this.view = view;
		view.setPresenter(this);
	}
	
	public void configure(FacetColumnResultRange facet, CallbackP<FacetColumnRequest> onFacetRequest, ColumnModel columnModel) {
		this.facet = facet;
		this.onFacetRequest = onFacetRequest;
		this.columnModel = columnModel;
		
		Number minMin = parseNumber(facet.getColumnMin());
		Number maxMax = parseNumber(facet.getColumnMax());
		double stepSize = (maxMax.doubleValue() - minMin.doubleValue()) / 200;
		if (ColumnType.INTEGER.equals(columnModel.getColumnType())) {
			stepSize = Math.round(stepSize);
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
