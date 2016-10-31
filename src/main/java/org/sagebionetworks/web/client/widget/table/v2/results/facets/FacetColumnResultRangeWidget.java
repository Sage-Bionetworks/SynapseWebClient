package org.sagebionetworks.web.client.widget.table.v2.results.facets;

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
	@Inject
	public FacetColumnResultRangeWidget(FacetColumnResultRangeView view) {
		this.view = view;
		view.setPresenter(this);
	}
	
	public void configure(FacetColumnResultRange facet, CallbackP<FacetColumnRequest> onFacetRequest) {
		this.facet = facet;
		this.onFacetRequest = onFacetRequest;
	}
	
	@Override
	public void onFacetChange() {
		FacetColumnRangeRequest facetColumnRangeRequest = new FacetColumnRangeRequest();
		facetColumnRangeRequest.setMin(view.getMin());
		facetColumnRangeRequest.setMax(view.getMax());
		onFacetRequest.invoke(facetColumnRangeRequest);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
