package org.sagebionetworks.web.client.widget.entity.controller;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProvenanceEditorWidgetViewImpl implements ProvenanceEditorWidgetView {

	public interface ProvenanceEditorWidgetViewImplUiBinder extends UiBinder<Widget, ProvenanceEditorWidgetViewImpl> {
	}

	@UiField
	Modal modal;

	@UiField
	SimplePanel synAlertPanel;

	@UiField
	TextBox editNameField;

	@UiField
	TextArea editDescriptionField;

	@UiField
	SimplePanel usedListPanel;

	@UiField
	SimplePanel executedListPanel;

	@UiField
	Button saveButton;

	@UiField
	Button cancelButton;

	@UiField
	SimplePanel entityFinderPanel;

	@UiField
	SimplePanel urlDialogPanel;



	Widget widget;
	Presenter presenter;

	@Inject
	public ProvenanceEditorWidgetViewImpl(ProvenanceEditorWidgetViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
		saveButton.addClickHandler(event -> {
			presenter.onSave();
		});
		cancelButton.addClickHandler(event -> {
			modal.hide();
		});
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setSynAlertWidget(IsWidget synAlert) {
		synAlertPanel.setWidget(synAlert);
	}

	@Override
	public void setName(String name) {
		editNameField.setText(name);
	}

	@Override
	public String getName() {
		return editNameField.getValue();
	}

	@Override
	public void setDescription(String description) {
		editDescriptionField.setText(description);
	}

	@Override
	public String getDescription() {
		return editDescriptionField.getValue();
	}

	@Override
	public void setUsedProvenanceList(IsWidget usedProvenanceList) {
		usedListPanel.setWidget(usedProvenanceList);
	}

	@Override
	public void setExecutedProvenanceList(IsWidget executedProvenanceList) {
		executedListPanel.setWidget(executedProvenanceList);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void clear() {
		editNameField.setText("");
		editDescriptionField.setText("");
	}

	@Override
	public void hide() {
		modal.hide();
	}

	@Override
	public void show() {
		modal.show();
	}

	@Override
	public void setEntityFinder(IsWidget entityFinder) {
		this.entityFinderPanel.setWidget(entityFinder);
	}

	@Override
	public void setURLDialog(IsWidget urlDialog) {
		this.urlDialogPanel.setWidget(urlDialog);
	}

}
