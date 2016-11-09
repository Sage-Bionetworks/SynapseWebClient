package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import java.util.HashSet;
import java.util.Set;

import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.repo.model.table.FacetColumnResultValueCount;
import org.sagebionetworks.repo.model.table.FacetColumnResultValues;
import org.sagebionetworks.repo.model.table.FacetColumnValuesRequest;
import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FacetColumnResultValuesWidget implements IsWidget, FacetColumnResultValuesView.Presenter {
	public static final String UNSPECIFIED = "(not set)";
	public static final String EMPTY_STRING = "(empty string)";
	FacetColumnResultValuesView view;
	FacetColumnResultValues facet;
	CallbackP<FacetColumnRequest> onFacetRequest;
	Set<String> facetValues;
	public static final int MAX_VISIBLE_FACET_VALUES=5;
	@Inject
	public FacetColumnResultValuesWidget(FacetColumnResultValuesView view) {
		this.view = view;
		view.setPresenter(this);
	}
	
	public void configure(FacetColumnResultValues facet, CallbackP<FacetColumnRequest> onFacetRequest) {
		view.clearValues();
		facetValues = new HashSet<String>();
		this.facet = facet;
		this.onFacetRequest = onFacetRequest;
		view.setColumnName(facet.getColumnName());
		int i = 0;
		for (FacetColumnResultValueCount valueCount : facet.getFacetValues()) {
			String displayValue = valueCount.getValue();
			if (displayValue == null) {
				displayValue = UNSPECIFIED;
			} else if (displayValue.trim().isEmpty()) {
				displayValue = EMPTY_STRING;
			}
			if (valueCount.getIsSelected()) {
				facetValues.add(valueCount.getValue());
			}
			if (i < MAX_VISIBLE_FACET_VALUES) {
				view.addValue(valueCount.getIsSelected(), displayValue, valueCount.getCount(), valueCount.getValue());	
			} else {
				view.addValueToOverflow(valueCount.getIsSelected(), displayValue, valueCount.getCount(), valueCount.getValue());
			}
			i++;
		}
		if (facet.getFacetValues().size() > MAX_VISIBLE_FACET_VALUES) {
			view.setShowAllButtonText("Show all " + facet.getFacetValues().size());
			view.setShowAllButtonVisible(true);
		} else {
			view.setShowAllButtonVisible(false);
		}
	}
	
	@Override
	public void onFacetChange(String facetValue) {
		FacetColumnValuesRequest facetColumnValuesRequest = new FacetColumnValuesRequest();
		facetColumnValuesRequest.setColumnName(facet.getColumnName());
		if (facetValues.contains(facetValue)) {
			facetValues.remove(facetValue);
		} else {
			facetValues.add(facetValue);
		}
		facetColumnValuesRequest.setFacetValues(facetValues);
		onFacetRequest.invoke(facetColumnValuesRequest);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
