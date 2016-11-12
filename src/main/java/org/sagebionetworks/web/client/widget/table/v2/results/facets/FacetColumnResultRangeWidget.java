package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import org.sagebionetworks.repo.model.table.FacetColumnRangeRequest;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.repo.model.table.FacetColumnResultRange;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FacetColumnResultRangeWidget implements IsWidget, FacetColumnResultRangeView.Presenter {
	public static final String MIN_MUST_BE_GREATER_THAN = "Min must be >= ";
	public static final String MAX_MUST_BE_LESS_THAN = "Max must be <= ";
	public static final String MIN_OR_MAX_MUST_BE_DEFINED = "Min or max must be defined.";
	public static final String MIN_MAX_MUST_BE_VALID_NUMBERS = "Min/max must be valid numbers.";
	FacetColumnResultRangeView view;
	FacetColumnResultRange facet;
	CallbackP<FacetColumnRequest> onFacetRequest;
	SynapseAlert synAlert;
	@Inject
	public FacetColumnResultRangeWidget(FacetColumnResultRangeView view, SynapseAlert synAlert) {
		this.view = view;
		this.synAlert = synAlert;
		view.setSynAlert(synAlert.asWidget());
		view.setPresenter(this);
	}
	
	public void configure(FacetColumnResultRange facet, CallbackP<FacetColumnRequest> onFacetRequest) {
		synAlert.clear();
		this.facet = facet;
		this.onFacetRequest = onFacetRequest;
		view.setColumnName(facet.getColumnName());
		if (facet.getSelectedMin() != null) {
			view.setMin(facet.getSelectedMin());
		}
		if (facet.getSelectedMax() != null) {
			view.setMax(facet.getSelectedMax());
		}
	}
	
	public Double getColumnMin() {
		String columnMinString = facet.getColumnMin();
		if (DisplayUtils.isDefined(columnMinString)) {
			return Double.parseDouble(columnMinString);
		}
		return null;
	}
	
	public Double getColumnMax() {
		String columnMaxString = facet.getColumnMax();
		if (DisplayUtils.isDefined(columnMaxString)) {
			return Double.parseDouble(columnMaxString);
		}
		return null;
	}
	
	public Double getNewMin() {
		String newMin = view.getMin();
		if (!DisplayUtils.isDefined(newMin)) {
			return getColumnMin();
		} else {
			return Double.parseDouble(newMin);
		}
	}
	
	public Double getNewMax() {
		String newMax = view.getMax();
		if (!DisplayUtils.isDefined(newMax)) {
			return getColumnMax();
		} else {
			return Double.parseDouble(newMax);
		}
	}
	
	public boolean isValidInput() {
		synAlert.clear();
		try {
			Double newMin = getNewMin();
			Double facetColumnMin = getColumnMin();
			if (newMin != null && facetColumnMin != null && newMin < facetColumnMin) {
				synAlert.showError(FacetColumnResultRangeWidget.MIN_MUST_BE_GREATER_THAN + facetColumnMin);
				return false;
			}
			
			Double newMax = getNewMax();
			Double facetColumnMax = getColumnMax();
			if (newMax != null && facetColumnMax != null && newMax > facetColumnMax) {
				synAlert.showError(FacetColumnResultRangeWidget.MAX_MUST_BE_LESS_THAN + facetColumnMax);
				return false;
			}
			
			if (newMin == null && newMax == null) {
				synAlert.showError(FacetColumnResultRangeWidget.MIN_OR_MAX_MUST_BE_DEFINED);
				return false;
			}
		} catch (NumberFormatException e) {
			synAlert.showError(FacetColumnResultRangeWidget.MIN_MAX_MUST_BE_VALID_NUMBERS);
			return false;
		}
		
		return true;
	}
	
	@Override
	public void onFacetChange() {
		if (isValidInput()) {
			FacetColumnRangeRequest facetColumnRangeRequest = new FacetColumnRangeRequest();
			facetColumnRangeRequest.setColumnName(facet.getColumnName());
			Double newMin = getNewMin();
			if (newMin != null) {
				facetColumnRangeRequest.setMin(Double.toString(newMin));	
			}
			Double newMax = getNewMax();
			if (newMax != null) {
				facetColumnRangeRequest.setMax(Double.toString(newMax));
			}
			onFacetRequest.invoke(facetColumnRangeRequest);
		}
	}
	
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
