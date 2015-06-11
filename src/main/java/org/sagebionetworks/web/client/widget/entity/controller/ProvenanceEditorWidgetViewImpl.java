package org.sagebionetworks.web.client.widget.entity.controller;

import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProvenanceEditorWidgetViewImpl extends Composite implements ProvenanceEditorWidgetView {

	public interface ProvenanceEditorWidgetViewImplUiBinder 
			extends UiBinder<Widget, ProvenanceEditorWidgetViewImpl> {}
	
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
	
	Widget widget;
	
	@Inject
	public ProvenanceEditorWidgetViewImpl(ProvenanceEditorWidgetViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void setVisible(boolean isVisible) {
		if (isVisible)
			modal.show();
		else
			modal.hide();
	}

	@Override
	public void setSynAlertWidget(Widget synAlert) {
		synAlertPanel.setWidget(synAlert);
	}

	@Override
	public void setName(String name) {
		editNameField.setText(name);
	}

	@Override
	public void setDescription(String description) {
		editDescriptionField.setText(description);		
	}
	
	@Override
	public void setUsedProvenanceList(Widget usedProvenanceList) {
		usedListPanel.setWidget(usedProvenanceList);
	}

	@Override
	public void setExecutedProvenanceList(Widget executedProvenanceList) {
		executedListPanel.setWidget(executedProvenanceList);
	}

}
