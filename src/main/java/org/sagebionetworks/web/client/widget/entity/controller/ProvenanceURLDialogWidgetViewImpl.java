package org.sagebionetworks.web.client.widget.entity.controller;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProvenanceURLDialogWidgetViewImpl extends Composite implements ProvenanceURLDialogWidgetView, IsWidget {

	public interface ProvenanceURLDialogWidgetViewImplUIBinder extends UiBinder<Widget, ProvenanceURLDialogWidgetViewImpl> {
	}

	@UiField
	Modal modal;

	@UiField
	TextBox editURLField;

	@UiField
	TextBox editNameField;

	@UiField
	Button saveButton;

	@UiField
	Button cancelButton;

	@UiField
	SimplePanel synAlertPanel;

	Widget widget;
	Presenter presenter;

	@Inject
	public ProvenanceURLDialogWidgetViewImpl(ProvenanceURLDialogWidgetViewImplUIBinder binder) {
		widget = binder.createAndBindUi(this);
		saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSave();
			}
		});
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.hide();
			}
		});
	}

	@Override
	public String getURLAddress() {
		return editURLField.getValue();
	}

	@Override
	public String getURLName() {
		return editNameField.getValue();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void show() {
		modal.show();
	}

	@Override
	public void hide() {
		modal.hide();
	}

	@Override
	public void clear() {
		editURLField.clear();
		editNameField.clear();
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setSynAlertWidget(IsWidget synAlert) {
		synAlertPanel.setWidget(synAlert);
	}

}
