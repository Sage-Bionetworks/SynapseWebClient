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
	boolean isShowingFacets;
	@Inject
	public FacetsWidget(DivView view, PortalGinInjector ginInjector) {
		this.view = view;
		this.ginInjector = ginInjector;
	}
	
	public void configure(List<FacetColumnResult> facets, CallbackP<FacetColumnRequest> facetChangedHandler, List<ColumnModel> types) {
		view.clear();
		isShowingFacets = false;
		if (facets != null) {
			Map<String, ColumnModel> columnName2ColumnModel = new HashMap<String, ColumnModel>();
			for (ColumnModel columnModel : types) {
				columnName2ColumnModel.put(columnModel.getName(), columnModel);
			}
			for (FacetColumnResult facet : facets) {
				ColumnModel cm = columnName2ColumnModel.get(facet.getColumnName());
				
				switch(facet.getFacetType()) {
					case enumeration:
						FacetColumnResultValues facetResultValues = (FacetColumnResultValues)facet;
						// if values are not set, then don't show the facet
						if (facetResultValues.getFacetValues() != null && facetResultValues.getFacetValues().size() > 0) {
							FacetColumnResultValuesWidget valuesWidget = ginInjector.getFacetColumnResultValuesWidget();
							boolean isUserIdColumnType = ColumnType.USERID.equals(cm.getColumnType());
							valuesWidget.configure(facetResultValues, isUserIdColumnType, facetChangedHandler);
							view.add(valuesWidget);
							isShowingFacets = true;
						}
						break;
					case range:
						FacetColumnResultRange rangeFacet = (FacetColumnResultRange)facet;
						// if there are no values found in the column, don't show the facet
						if (rangeFacet.getColumnMin() != null) {
							isShowingFacets = true;
							if (ColumnType.INTEGER.equals(cm.getColumnType())) {
								FacetColumnResultSliderRangeWidget rangeWidget = ginInjector.getFacetColumnResultSliderRangeWidget();
								rangeWidget.configure(rangeFacet, facetChangedHandler);
								view.add(rangeWidget);	
							} else if (ColumnType.DOUBLE.equals(cm.getColumnType())) {
								FacetColumnResultRangeWidget rangeWidget = ginInjector.getFacetColumnResultRangeWidget();
								rangeWidget.configure(rangeFacet, facetChangedHandler);
								view.add(rangeWidget);
							} else if (ColumnType.DATE.equals(cm.getColumnType())) {
								FacetColumnResultDateRangeWidget rangeWidget = ginInjector.getFacetColumnResultDateRangeWidget();
								rangeWidget.configure(rangeFacet, facetChangedHandler);
								view.add(rangeWidget);
							}
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
	
	public boolean isShowingFacets() {
		return isShowingFacets;
	}
}
