package org.sagebionetworks.web.client.widget.table.modal.fileview;

import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.InlineCheckBox;
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
	
	@UiField
	FormGroup viewOptionsUI;
	@UiField
	InlineCheckBox includeTablesCb;
	
	Widget widget;
	Presenter p;
	
	@Inject
	public CreateTableViewWizardStep1ViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
		includeTablesCb.addClickHandler(event -> {
			if (includeTablesCb.getValue()) {
				p.onSelectFilesAndTablesView();
			} else {
				p.onSelectFilesOnlyView();
			}
			
		});
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
	
	@Override
	public void setFileViewTypeSelectionVisible(boolean visible) {
		viewOptionsUI.setVisible(visible);
	}
	@Override
	public void setPresenter(Presenter presenter) {
		p = presenter;
	}
}
