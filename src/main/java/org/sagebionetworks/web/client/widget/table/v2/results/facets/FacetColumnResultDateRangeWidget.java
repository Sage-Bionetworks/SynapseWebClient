package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import java.util.Date;

import org.sagebionetworks.repo.model.table.FacetColumnRangeRequest;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.repo.model.table.FacetColumnResultRange;
import org.sagebionetworks.repo.model.table.TableConstants;
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
		
		boolean isAny = facet.getSelectedMin() == null && facet.getSelectedMax() == null;
		boolean isNotSetFilter = (facet.getSelectedMin() != null && facet.getSelectedMin().equals(TableConstants.NULL_VALUE_KEYWORD)) ||
				(facet.getSelectedMax() != null && facet.getSelectedMax().equals(TableConstants.NULL_VALUE_KEYWORD));
		if (isAny) {
			view.setIsAnyValue();
		} else if (isNotSetFilter) {
			view.setIsNotSet();
		} else {
			Date min = parseDate(facet.getSelectedMin());
			if (min != null) {
				view.setMin(min);
			}
			Date max = parseDate(facet.getSelectedMax());
			if (max != null) {
				view.setMax(max);
			}
			view.setIsRange();
		}
	}
	
	public static Date parseDate(String s) {
		Date number = null;
		if (s != null) {
			number = new Date(Long.parseLong(s));
		}
	    return number;
	}
	
	public Long getColumnMin() {
		String columnMinString = facet.getColumnMin();
		if (DisplayUtils.isDefined(columnMinString)) {
			return Long.parseLong(columnMinString);
		}
		return null;
	}
	
	public Long getColumnMax() {
		String columnMaxString = facet.getColumnMax();
		if (DisplayUtils.isDefined(columnMaxString)) {
			return Long.parseLong(columnMaxString);
		}
		return null;
	}
	
	public Long getNewMin() {
		Date newMin = view.getMin();
		if (newMin == null) {
			return getColumnMin();
		} else {
			return newMin.getTime();
		}
	}
	
	public Long getNewMax() {
		Date newMax = view.getMax();
		if (newMax == null) {
			return getColumnMax();
		} else {
			return newMax.getTime();
		}
	}
	
	@Override
	public void onFacetChange() {
		FacetColumnRangeRequest facetColumnRangeRequest = new FacetColumnRangeRequest();
		facetColumnRangeRequest.setColumnName(facet.getColumnName());
		
		if (view.isNotSet()) {
			facetColumnRangeRequest.setMin(TableConstants.NULL_VALUE_KEYWORD);
			facetColumnRangeRequest.setMax(TableConstants.NULL_VALUE_KEYWORD);
		} else if (!view.isAnyValue()) {
			Long newMin = getNewMin();
			if (newMin != null) {
				facetColumnRangeRequest.setMin(Long.toString(newMin));	
			}
			Long newMax = getNewMax();
			if (newMax != null) {
				facetColumnRangeRequest.setMax(Long.toString(newMax));
			}
		}
		
		onFacetRequest.invoke(facetColumnRangeRequest);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
