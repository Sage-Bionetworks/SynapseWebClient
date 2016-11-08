package org.sagebionetworks.web.client.widget.table.v2.results.facets;

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
	
	Widget w;
	Presenter presenter;
	@Inject
	public FacetColumnResultValuesViewImpl(Binder binder){
		w = binder.createAndBindUi(this);
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
	public void addValue(boolean isSelected, final String facetValue, Long count) {
		FacetColumnResultValueWidget valueWidget = new FacetColumnResultValueWidget();
		valueWidget.setIsSelected(isSelected);
		valueWidget.setValueName(facetValue);
		valueWidget.setCount(count);
		valueWidget.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onFacetChange(facetValue);
			}
		});
		facetValues.add(valueWidget);
	}
	
	@Override
	public void clearValues() {
		facetValues.clear();
	}
	
	@Override
	public void setColumnName(String name) {
		columnName.setText(name);
	}
}
