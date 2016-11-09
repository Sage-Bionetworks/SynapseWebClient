package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import java.util.Date;

import org.sagebionetworks.repo.model.table.FacetColumnRangeRequest;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.repo.model.table.FacetColumnResultRange;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FacetColumnResultDateRangeWidget implements IsWidget, FacetColumnResultDateRangeView.Presenter {
	FacetColumnResultDateRangeView view;
	FacetColumnResultRange facet;
	CallbackP<FacetColumnRequest> onFacetRequest;
	SynapseAlert synAlert;
	@Inject
	public FacetColumnResultDateRangeWidget(FacetColumnResultDateRangeView view, SynapseAlert synAlert) {
		this.view = view;
		this.synAlert = synAlert;
		view.setPresenter(this);
		view.setSynAlert(synAlert.asWidget());
	}
	
	public void configure(FacetColumnResultRange facet, CallbackP<FacetColumnRequest> onFacetRequest) {
		synAlert.clear();
		this.facet = facet;
		this.onFacetRequest = onFacetRequest;
		view.setColumnName(facet.getColumnName());
		Date min = parseDate(facet.getSelectedMin());
		if (min != null) {
			view.setMin(min);
		}
		Date max = parseDate(facet.getSelectedMin());
		if (max != null) {
			view.setMax(max);
		}
	}
	
	public static Date parseDate(String s) {
		Date number = null;
		if (s != null) {
			number = new Date(Long.parseLong(s));
		}
	    return number;
	}
	

	public boolean isValidInput() {
		synAlert.clear();
		try {
			Date newMin = view.getMin();
			String columnMinString = facet.getColumnMin();
			if (newMin != null) {
				if (DisplayUtils.isDefined(columnMinString)) {
					double columnMin = Long.parseLong(columnMinString);
					if (newMin.getTime() < columnMin) {
						synAlert.showError(FacetColumnResultRangeWidget.MIN_MUST_BE_GREATER_THAN + columnMinString);
						return false;
					}
				}
			}
			
			Date newMax = view.getMax();
			String columnMaxString = facet.getColumnMax();
			if (newMax != null) {
				if (DisplayUtils.isDefined(columnMaxString)) {
					double columnMax = Long.parseLong(columnMaxString);
					if (newMax.getTime() > columnMax) {
						synAlert.showError(FacetColumnResultRangeWidget.MAX_MUST_BE_LESS_THAN + columnMaxString);
						return false;
					}
				}
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
			Date newMin = view.getMin();
			if (newMin != null) {
				facetColumnRangeRequest.setMin(Long.toString(newMin.getTime()));	
			}
			Date newMax = view.getMax();
			if (newMax != null) {
				facetColumnRangeRequest.setMax(Long.toString(newMax.getTime()));
			}
			onFacetRequest.invoke(facetColumnRangeRequest);
		}
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
