package org.sagebionetworks.web.client.widget.table.modal.fileview;

import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CreateTableViewWizardStep1ViewImpl implements CreateTableViewWizardStep1View {

	public interface Binder extends UiBinder<Widget, CreateTableViewWizardStep1ViewImpl> {}
	
	@UiField
	TextBox nameField;
	@UiField
	SimplePanel scopeContainer;
	@UiField
	FormGroup scopeUI;
	
	Widget widget;
	
	@Inject
	public CreateTableViewWizardStep1ViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
	}
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public String getName() {
		return nameField.getText();
	}
	
	@Override
	public void setName(String name) {
		nameField.setText(name);
	}
	
	@Override
	public void setScopeWidget(IsWidget scopeWidget) {
		scopeContainer.clear();
		scopeContainer.setWidget(scopeWidget);
	}

	@Override
	public void setScopeWidgetVisible(boolean visible) {
		scopeUI.setVisible(visible);
	}
}
