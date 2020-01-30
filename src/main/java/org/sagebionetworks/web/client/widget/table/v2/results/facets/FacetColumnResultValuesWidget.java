package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import static org.sagebionetworks.repo.model.table.TableConstants.NULL_VALUE_KEYWORD;
import java.util.HashSet;
import java.util.Set;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.repo.model.table.FacetColumnResultValueCount;
import org.sagebionetworks.repo.model.table.FacetColumnResultValues;
import org.sagebionetworks.repo.model.table.FacetColumnValuesRequest;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRenderer;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.UserIdCellRenderer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FacetColumnResultValuesWidget implements IsWidget, FacetColumnResultValuesView.Presenter {
	public static final String SHOW_ALL = "Show all ";
	public static final String UNSPECIFIED = "Not set";
	public static final String EMPTY_STRING = "(empty string)";

	FacetColumnResultValuesView view;
	FacetColumnResultValues facet;
	CallbackP<FacetColumnRequest> onFacetRequest;
	Set<String> facetValues;
	public static final int MAX_VISIBLE_FACET_VALUES = 5;
	PortalGinInjector ginInjector;
	ClickHandler doNothingClickHandler;


	@Inject
	public FacetColumnResultValuesWidget(FacetColumnResultValuesView view, PortalGinInjector ginInjector) {
		this.view = view;
		this.ginInjector = ginInjector;
		view.setPresenter(this);
		doNothingClickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {}
		};
	}

	public void configure(FacetColumnResultValues facet, ColumnType columnType, CallbackP<FacetColumnRequest> onFacetRequest) {
		boolean isUserIdColumnType = ColumnType.USERID.equals(columnType);
		boolean isEntityIdColumnType = ColumnType.ENTITYID.equals(columnType);
		view.clearValues();
		facetValues = new HashSet<String>();
		this.facet = facet;
		this.onFacetRequest = onFacetRequest;
		view.setColumnName(facet.getColumnName());
		int i = 0;
		int max = MAX_VISIBLE_FACET_VALUES;
		for (int j = MAX_VISIBLE_FACET_VALUES; j < facet.getFacetValues().size(); j++) {
			// if any facet is selected beyond the max visible count, then show them all
			if (facet.getFacetValues().get(j).getIsSelected()) {
				max = Integer.MAX_VALUE;
				break;
			}
		}

		for (FacetColumnResultValueCount valueCount : facet.getFacetValues()) {
			if (valueCount.getIsSelected()) {
				facetValues.add(valueCount.getValue());
			}

			Widget displayWidget;

			if (valueCount.getValue() == null || NULL_VALUE_KEYWORD.equals(valueCount.getValue())) {
				displayWidget = view.getSpanWithText(UNSPECIFIED);
			} else if (isUserIdColumnType) {
				displayWidget = getUserBadge(valueCount.getValue());
			} else if (isEntityIdColumnType) {
				displayWidget = getEntityBadge(valueCount.getValue());
			} else {
				displayWidget = view.getSpanWithText(valueCount.getValue());
			}

			if (i < max) {
				view.addValue(valueCount.getIsSelected(), displayWidget, valueCount.getCount(), valueCount.getValue());
			} else {
				view.addValueToOverflow(valueCount.getIsSelected(), displayWidget, valueCount.getCount(), valueCount.getValue());
			}
			i++;
		}
		if (facet.getFacetValues().size() > max) {
			view.setShowAllButtonText(SHOW_ALL + facet.getFacetValues().size());
			view.setShowAllButtonVisible(true);
		} else {
			view.setShowAllButtonVisible(false);
		}
	}

	public Widget getUserBadge(String userId) {
		UserIdCellRenderer userBadge = ginInjector.getUserIdCellRenderer();
		userBadge.setValue(userId, doNothingClickHandler);
		return userBadge.asWidget();
	}

	public Widget getEntityBadge(String entityId) {
		EntityIdCellRenderer entityBadge = ginInjector.getEntityIdCellRenderer();
		boolean hideIfLoadError = true;
		entityBadge.setValue(entityId, doNothingClickHandler, hideIfLoadError);
		return entityBadge.asWidget();
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
