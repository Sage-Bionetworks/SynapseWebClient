package org.sagebionetworks.web.client.widget.table.v2.results.facets;

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
	public FacetColumnResultRangeWidget(SynapseAlert synAlert) {
		this.synAlert = synAlert;
	}

	public void configure(FacetColumnResultRangeView view, FacetColumnResultRange facet, CallbackP<FacetColumnRequest> onFacetRequest) {
		this.view = view;
		view.setSynAlert(synAlert.asWidget());
		view.setPresenter(this);
		synAlert.clear();
		this.facet = facet;
		this.onFacetRequest = onFacetRequest;
		view.setColumnName(facet.getColumnName());
		view.setLowerBound(facet.getColumnMin());
		view.setUpperBound(facet.getColumnMax());
		// by default, set the range to the lowerbound/upperbound
		view.setMin(facet.getColumnMin());
		view.setMax(facet.getColumnMax());
		boolean isAny = facet.getSelectedMin() == null && facet.getSelectedMax() == null;
		boolean isNotSetFilter = (facet.getSelectedMin() != null && facet.getSelectedMin().equals(TableConstants.NULL_VALUE_KEYWORD)) || (facet.getSelectedMax() != null && facet.getSelectedMax().equals(TableConstants.NULL_VALUE_KEYWORD));
		if (isAny) {
			view.setIsAnyValue();
		} else if (isNotSetFilter) {
			view.setIsNotSet();
		} else {
			if (facet.getSelectedMin() != null) {
				view.setMin(facet.getSelectedMin());
			}
			if (facet.getSelectedMax() != null) {
				view.setMax(facet.getSelectedMax());
			}
			view.setIsRange();
		}
	}

	public String getNewMin() {
		String newMin = view.getMin();
		if (!DisplayUtils.isDefined(newMin)) {
			return facet.getColumnMin();
		} else {
			return newMin;
		}
	}

	public String getNewMax() {
		String newMax = view.getMax();
		if (!DisplayUtils.isDefined(newMax)) {
			return facet.getColumnMax();
		} else {
			return newMax;
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
			String newMin = getNewMin();
			if (newMin != null) {
				facetColumnRangeRequest.setMin(newMin);
			}
			String newMax = getNewMax();
			if (newMax != null) {
				facetColumnRangeRequest.setMax(newMax);
			}
		}
		// note, if isAnyValue, request contains null min and null max
		onFacetRequest.invoke(facetColumnRangeRequest);
	}


	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
