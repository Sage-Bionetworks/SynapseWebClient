package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
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
		if (facets != null) {
			Map<String, ColumnModel> columnName2ColumnModel = new HashMap<String, ColumnModel>();
			for (ColumnModel columnModel : types) {
				columnName2ColumnModel.put(columnModel.getName(), columnModel);
			}
			for (FacetColumnResult facet : facets) {
				switch(facet.getFacetType()) {
					case enumeration:
						FacetColumnResultValuesWidget valuesWidget = ginInjector.getFacetColumnResultValuesWidget();
						valuesWidget.configure((FacetColumnResultValues)facet, facetChangedHandler);
						view.add(valuesWidget);
						break;
					case range:
						ColumnModel cm = columnName2ColumnModel.get(facet.getColumnName());
						if (ColumnType.INTEGER.equals(cm.getColumnType())) {
							FacetColumnResultSliderRangeWidget rangeWidget = ginInjector.getFacetColumnResultSliderRangeWidget();
							rangeWidget.configure((FacetColumnResultRange)facet, facetChangedHandler);
							view.add(rangeWidget);	
						} else if (ColumnType.DOUBLE.equals(cm.getColumnType())) {
							FacetColumnResultRangeWidget rangeWidget = ginInjector.getFacetColumnResultRangeWidget();
							rangeWidget.configure((FacetColumnResultRange)facet, facetChangedHandler);
							view.add(rangeWidget);
						} else if (ColumnType.DATE.equals(cm.getColumnType())) {
							FacetColumnResultDateRangeWidget rangeWidget = ginInjector.getFacetColumnResultDateRangeWidget();
							rangeWidget.configure((FacetColumnResultRange)facet, facetChangedHandler);
							view.add(rangeWidget);
						}
						
						break;
					default:
						break;
				}
			}
		}
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
