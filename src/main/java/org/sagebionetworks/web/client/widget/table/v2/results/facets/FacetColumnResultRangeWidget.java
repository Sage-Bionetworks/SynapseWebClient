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
		this.facet = facet;
		this.onFacetRequest = onFacetRequest;
		view.setColumnName(facet.getColumnName());
		if (facet.getSelectedMin() != null) {
			view.setMin(facet.getSelectedMin());
		}
		if (facet.getSelectedMax() != null) {
			view.setMin(facet.getSelectedMax());
		}
	}
	
	public boolean isValidInput() {
		synAlert.clear();
		try {
			String minString = view.getMin();
			String columnMinString = facet.getColumnMin();
			if (DisplayUtils.isDefined(minString)) {
				double min = Double.parseDouble(view.getMin());
				if (DisplayUtils.isDefined(columnMinString)) {
					double columnMin = Double.parseDouble(columnMinString);
					if (min < columnMin) {
						synAlert.showError(MIN_MUST_BE_GREATER_THAN + columnMinString);
						return false;
					}
				}
			}
			
			String maxString = view.getMax();
			String columnMaxString = facet.getColumnMax();
			if (DisplayUtils.isDefined(maxString)) {
				double max = Double.parseDouble(view.getMax());
				if (DisplayUtils.isDefined(columnMaxString)) {
					double columnMax = Double.parseDouble(columnMaxString);
					if (max > columnMax) {
						synAlert.showError(MAX_MUST_BE_LESS_THAN + columnMaxString);
						return false;
					}
				}
			}
			
			if (!DisplayUtils.isDefined(minString) && !DisplayUtils.isDefined(maxString)) {
				synAlert.showError(MIN_OR_MAX_MUST_BE_DEFINED);
				return false;
			}
		} catch (NumberFormatException e) {
			synAlert.showError(MIN_MAX_MUST_BE_VALID_NUMBERS);
			return false;
		}
		
		return true;
	}
	
	@Override
	public void onFacetChange() {
		if (isValidInput()) {
			FacetColumnRangeRequest facetColumnRangeRequest = new FacetColumnRangeRequest();
			facetColumnRangeRequest.setMin(view.getMin());
			facetColumnRangeRequest.setMax(view.getMax());
			onFacetRequest.invoke(facetColumnRangeRequest);
		}
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
