package org.sagebionetworks.web.client.widget.table.v2.results.facets;

import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetColumnResultValuesView.Presenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FacetColumnResultValueWidget implements IsWidget {

	interface FacetColumnResultValueWidgetBinder extends UiBinder<Widget, FacetColumnResultValueWidget> {}
	private static FacetColumnResultValueWidgetBinder binder = GWT
			.create(FacetColumnResultValueWidgetBinder.class);
	@UiField
	CheckBox select;
	@UiField
	Span valueName;
	@UiField
	Span count;
	@UiField
	FocusPanel valueNameContainer;
	@UiField
	FocusPanel countContainer;
	
	Widget w;
	Presenter presenter;
	ClickHandler clickHandler;
	@Inject
	public FacetColumnResultValueWidget(){
		w = binder.createAndBindUi(this);
		ClickHandler toggleSelect = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				select.setValue(!select.getValue());
				clickHandler.onClick(event);
			}
		};
		valueNameContainer.addClickHandler(toggleSelect);
		countContainer.addClickHandler(toggleSelect);
	}
	
	@Override
	public Widget asWidget() {
		return w;
	}
	
	public void setIsSelected(boolean isSelected) {
		select.setValue(isSelected);
	}
	public void setValueName(String name) {
		valueName.setText(name);
	}
	public void setCount(Long c) {
		count.setText(c.toString());
	}
	
	public void addClickHandler(ClickHandler clickHandler) {
		this.clickHandler = clickHandler;
		select.addClickHandler(clickHandler);
	}
}
