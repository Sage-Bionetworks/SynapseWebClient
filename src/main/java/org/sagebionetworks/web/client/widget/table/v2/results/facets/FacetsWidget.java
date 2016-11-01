package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.table.ColumnModel;
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
	@Inject
	public FacetsWidget(DivView view, PortalGinInjector ginInjector) {
		this.view = view;
		this.ginInjector = ginInjector;
	}
	
	public void configure(List<FacetColumnResult> facets, CallbackP<FacetColumnRequest> facetChangedHandler, List<ColumnModel> types) {
		view.clear();
		Map<String, ColumnModel> columnName2ColumnModel = new HashMap<String, ColumnModel>();
		for (ColumnModel columnModel : types) {
			columnName2ColumnModel.put(columnModel.getName(), columnModel);
		}
		for (FacetColumnResult facet : facets) {
			switch(facet.getFacetType()) {
				case enumeration:
					FacetColumnResultValuesWidget valuesWidget = ginInjector.getFacetColumnResultValuesWidget();
					valuesWidget.configure((FacetColumnResultValues)facet, facetChangedHandler, columnName2ColumnModel.get(facet.getColumnName()));
					view.add(valuesWidget);
					break;
				case range:
					FacetColumnResultRangeWidget rangeWidget = ginInjector.getFacetColumnResultRangeWidget();
					rangeWidget.configure((FacetColumnResultRange)facet, facetChangedHandler, columnName2ColumnModel.get(facet.getColumnName()));
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
