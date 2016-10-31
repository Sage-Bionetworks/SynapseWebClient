package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import java.util.List;

import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.repo.model.table.FacetColumnResult;
import org.sagebionetworks.repo.model.table.FacetColumnResultRange;
import org.sagebionetworks.repo.model.table.FacetColumnResultValues;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.DivView;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FacetsWidget implements IsWidget {
	DivView view;
	PortalGinInjector ginInjector;
	CallbackP<FacetColumnRequest> facetChangedHandler;
	@Inject
	public FacetsWidget(DivView view, PortalGinInjector ginInjector) {
		this.view = view;
		this.ginInjector = ginInjector;
		facetChangedHandler = new CallbackP<FacetColumnRequest>() {
			@Override
			public void invoke(FacetColumnRequest param) {
				
			}
		};
	}
	
	public void configure(List<FacetColumnResult> facets) {
		view.clear();
		for (FacetColumnResult facet : facets) {
			switch(facet.getFacetType()) {
				case enumeration:
					FacetColumnResultValuesWidget valuesWidget = ginInjector.getFacetColumnResultValuesWidget();
					valuesWidget.configure((FacetColumnResultValues)facet, facetChangedHandler);
					view.add(valuesWidget);
					break;
				case range:
					FacetColumnResultRangeWidget rangeWidget = ginInjector.getFacetColumnResultRangeWidget();
					rangeWidget.configure((FacetColumnResultRange)facet, facetChangedHandler);
					view.add(rangeWidget);
					break;
				default:
					break;
			}
		}
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
