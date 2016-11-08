package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Strong;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FacetColumnResultValuesViewImpl implements FacetColumnResultValuesView {
	
	public interface Binder extends UiBinder<Widget, FacetColumnResultValuesViewImpl> {	}
	@UiField
	Strong columnName;
	@UiField
	Div facetValues;
	@UiField
	Button showAllButton;
	@UiField
	Div overflowFacetValues;
	Widget w;
	Presenter presenter;
	@Inject
	public FacetColumnResultValuesViewImpl(Binder binder){
		w = binder.createAndBindUi(this);
		showAllButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showAllButton.setVisible(false);
				overflowFacetValues.setVisible(true);
			}
		});
	}

	@Override
	public Widget asWidget() {
		return w;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void addValue(boolean isSelected, String facetValue, Long count) {
		facetValues.add(getNewFacetColumnResultValueWidget(isSelected, facetValue, count));
	}
	
	@Override
	public void addValueToOverflow(boolean isSelected, String facetValue, Long count) {
		overflowFacetValues.add(getNewFacetColumnResultValueWidget(isSelected, facetValue, count));
	}
	
	@Override
	public void setShowAllButtonText(String text) {
		showAllButton.setText(text);
	}
	
	@Override
	public void setShowAllButtonVisible(boolean visible) {
		showAllButton.setVisible(visible);
	}
	
	private FacetColumnResultValueWidgetImpl getNewFacetColumnResultValueWidget(boolean isSelected, final String facetValue, Long count){
		FacetColumnResultValueWidgetImpl valueWidget = new FacetColumnResultValueWidgetImpl();
		valueWidget.setIsSelected(isSelected);
		valueWidget.setValueName(facetValue);
		valueWidget.setCount(count);
		valueWidget.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onFacetChange(facetValue);
			}
		});
		return valueWidget;
	}
	
	@Override
	public void clearValues() {
		facetValues.clear();
		overflowFacetValues.clear();
		overflowFacetValues.setVisible(false);
	}
	
	@Override
	public void setColumnName(String name) {
		columnName.setText(name);
	}
}
