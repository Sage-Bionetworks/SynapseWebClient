package org.sagebionetworks.web.client.widget.table.modal.fileview;

import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CreateFileViewWizardStep1ViewImpl implements CreateFileViewWizardStep1View {

	public interface Binder extends UiBinder<Widget, CreateFileViewWizardStep1ViewImpl> {}
	
	@UiField
	TextBox nameField;
	@UiField
	SimplePanel scopeContainer;
	
	Widget widget;
	
	@Inject
	public CreateFileViewWizardStep1ViewImpl(Binder binder){
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

}
