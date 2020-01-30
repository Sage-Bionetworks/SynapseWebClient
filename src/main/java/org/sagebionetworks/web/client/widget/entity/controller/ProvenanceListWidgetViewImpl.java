package org.sagebionetworks.web.client.widget.entity.controller;

import org.gwtbootstrap3.client.ui.Button;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProvenanceListWidgetViewImpl extends Composite implements ProvenanceListWidgetView {

	public interface ProvenanceListWidgetViewImplUiBinder extends UiBinder<Widget, ProvenanceListWidgetViewImpl> {
	}

	@UiField
	TBody provTableBody;

	@UiField
	Button addEntityButton;

	@UiField
	Button addURLButton;

	@UiField
	SimplePanel entityFinderPanel;

	@UiField
	SimplePanel urlDialogPanel;

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
	public void addRow(IsWidget newRow) {
		provTableBody.add(newRow);
	}

	@Override
	public void removeRow(IsWidget toRemove) {
		provTableBody.remove(toRemove);
	}

	@Override
	public void setEntityFinder(IsWidget entityFinder) {
		entityFinderPanel.setWidget(entityFinder);
	}

	@Override
	public void setURLDialog(IsWidget urlDialog) {
		urlDialogPanel.setWidget(urlDialog);
	}

	@Override
	public void clear() {
		provTableBody.clear();
	}

}
