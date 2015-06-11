package org.sagebionetworks.web.client.widget.entity.controller;

import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProvenanceListWidgetViewImpl extends Composite implements ProvenanceListWidgetView {

	public interface ProvenanceListWidgetViewImplUiBinder extends UiBinder<Widget, ProvenanceListWidgetViewImpl> {}
	
	@UiField
	FlowPanel itemList;
	
	@UiField
	Button addEntityButton;
	
	@UiField
	Button addURLButton;
	
	Presenter presenter;
	Widget widget;
	
	@Inject
	public ProvenanceListWidgetViewImpl(ProvenanceListWidgetViewImplUiBinder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
		addEntityButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.addEntityRow();
			}
		});
		addURLButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.addURLRow();
			}
		});
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void addRow(Widget newRow) {
		itemList.add(newRow);
	}

}
