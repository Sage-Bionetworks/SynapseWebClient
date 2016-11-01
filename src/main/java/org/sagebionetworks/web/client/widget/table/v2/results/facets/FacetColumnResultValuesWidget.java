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
	FacetColumnResultValuesView view;
	FacetColumnResultValues facet;
	CallbackP<FacetColumnRequest> onFacetRequest;
	Set<String> facetValues;
	@Inject
	public FacetColumnResultValuesWidget(FacetColumnResultValuesView view) {
		this.view = view;
		view.setPresenter(this);
	}
	
	public void configure(FacetColumnResultValues facet, CallbackP<FacetColumnRequest> onFacetRequest) {
		facetValues = new HashSet<String>();
		this.facet = facet;
		this.onFacetRequest = onFacetRequest;
		for (FacetColumnResultValueCount valueCount : facet.getFacetValues()) {
			if (valueCount.getIsSelected()) {
				facetValues.add(valueCount.getValue());
			}
			view.addValue(valueCount.getIsSelected(), valueCount.getValue(), valueCount.getCount());
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
