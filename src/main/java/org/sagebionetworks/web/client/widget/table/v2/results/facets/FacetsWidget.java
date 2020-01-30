package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.repo.model.table.FacetColumnResult;
import org.sagebionetworks.repo.model.table.FacetColumnResultRange;
import org.sagebionetworks.repo.model.table.FacetColumnResultValues;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryBundleRequest;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.SingleButtonView;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryBundleUtils;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWidget;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FacetsWidget implements IsWidget {
	DivView view;
	PortalGinInjector ginInjector;
	SingleButtonView clearButton;
	Callback resetFacetsHandler;

	@Inject
	public FacetsWidget(DivView view, SingleButtonView clearButton, PortalGinInjector ginInjector) {
		this.view = view;
		view.addStyleName("facetsWidget");
		this.clearButton = clearButton;
		clearButton.setButtonText("Clear all");
		clearButton.setButtonSize(ButtonSize.EXTRA_SMALL);
		clearButton.setButtonType(ButtonType.DANGER);
		clearButton.addStyleNames("clearFacetsButton margin-top-20");
		clearButton.setButtonIcon(IconType.FILTER);
		clearButton.setPresenter(() -> {
			// on click
			resetFacetsHandler.invoke();
		});
		this.ginInjector = ginInjector;
	}

	public void configure(Query query, CallbackP<FacetColumnRequest> facetChangedHandler, Callback resetFacetsHandler) {
		view.clear();
		AsynchronousProgressWidget progress = ginInjector.creatNewAsynchronousProgressWidget();
		QueryBundleRequest qbr = new QueryBundleRequest();
		long partMask = TableQueryResultWidget.BUNDLE_MASK_QUERY_FACETS | TableQueryResultWidget.BUNDLE_MASK_QUERY_COLUMN_MODELS;
		qbr.setPartMask(partMask);
		qbr.setQuery(query);
		qbr.setEntityId(QueryBundleUtils.getTableId(query));
		view.add(progress);
		progress.startAndTrackJob("", false, AsynchType.TableQuery, qbr, new AsynchronousProgressHandler() {
			@Override
			public void onFailure(Throwable failure) {
				view.clear();
				SynapseAlert synAlert = ginInjector.getSynapseAlertWidget();
				view.add(synAlert);
				synAlert.handleException(failure);
			}

			@Override
			public void onComplete(AsynchronousResponseBody response) {
				QueryResultBundle resultBundle = (QueryResultBundle) response;
				configure(resultBundle.getFacets(), facetChangedHandler, resultBundle.getColumnModels(), resetFacetsHandler);
			}

			@Override
			public void onCancel() {
				view.clear();
			}
		});
	}

	public void configure(List<FacetColumnResult> facets, CallbackP<FacetColumnRequest> facetChangedHandler, List<ColumnModel> types, Callback resetFacetsHandler) {
		view.clear();
		this.resetFacetsHandler = resetFacetsHandler;
		if (facets != null) {
			Map<String, ColumnModel> columnName2ColumnModel = new HashMap<String, ColumnModel>();
			for (ColumnModel columnModel : types) {
				columnName2ColumnModel.put(columnModel.getName(), columnModel);
			}
			for (FacetColumnResult facet : facets) {
				ColumnModel cm = columnName2ColumnModel.get(facet.getColumnName());
				if (cm != null) {
					switch (facet.getFacetType()) {
						case enumeration:
							FacetColumnResultValues facetResultValues = (FacetColumnResultValues) facet;
							// if values are not set, then don't show the facet
							if (facetResultValues.getFacetValues() != null && facetResultValues.getFacetValues().size() > 0) {
								FacetColumnResultValuesWidget valuesWidget = ginInjector.getFacetColumnResultValuesWidget();
								valuesWidget.configure(facetResultValues, cm.getColumnType(), facetChangedHandler);
								view.add(valuesWidget);
							}
							break;
						case range:
							FacetColumnResultRange rangeFacet = (FacetColumnResultRange) facet;
							// if there are no values found in the column, don't show the facet
							if (rangeFacet.getColumnMin() != null) {

								FacetColumnResultRangeWidget rangeWidget = ginInjector.getFacetColumnResultRangeWidget();
								FacetColumnResultRangeView rangeView = null;

								if (ColumnType.INTEGER.equals(cm.getColumnType())) {
									rangeView = ginInjector.getFacetColumnResultSliderRangeViewImpl();
								} else if (ColumnType.DOUBLE.equals(cm.getColumnType())) {
									rangeView = ginInjector.getFacetColumnResultRangeViewImpl();
								} else if (ColumnType.DATE.equals(cm.getColumnType())) {
									rangeView = ginInjector.getFacetColumnResultDateRangeViewImpl();
								}

								if (rangeView != null) {
									rangeWidget.configure(rangeView, rangeFacet, facetChangedHandler);
									view.add(rangeWidget);
								}
							}

							break;
						default:
							break;
					}
				}
			}
			if (facets.size() > 0) {
				view.add(clearButton);
			}
		}
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

}
