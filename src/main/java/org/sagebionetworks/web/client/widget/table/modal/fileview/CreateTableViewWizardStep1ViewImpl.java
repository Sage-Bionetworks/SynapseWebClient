package org.sagebionetworks.web.client.widget.table.modal.fileview;

import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CreateTableViewWizardStep1ViewImpl implements CreateTableViewWizardStep1View {

	public interface Binder extends UiBinder<Widget, CreateTableViewWizardStep1ViewImpl> {
	}

	@UiField
	TextBox nameField;
	@UiField
	SimplePanel scopeContainer;
	@UiField
	FormGroup scopeUI;
	@UiField
	Div viewOptionsContainer;
	Widget widget;
	Presenter p;
	FileViewOptions viewOptions;

	@Inject
	public CreateTableViewWizardStep1ViewImpl(Binder binder, FileViewOptions viewOptions) {
		widget = binder.createAndBindUi(this);
		this.viewOptions = viewOptions;
		viewOptionsContainer.add(viewOptions);
		viewOptions.addClickHandler(event -> {
			p.updateViewTypeMask();
		});
	}

	@Override
	public boolean isFileSelected() {
		return viewOptions.isIncludeFiles();
	}

	@Override
	public void setIsFileSelected(boolean value) {
		viewOptions.setIsIncludeFiles(value);
	}

	@Override
	public boolean isFolderSelected() {
		return viewOptions.isIncludeFolders();
	}

	@Override
	public void setIsFolderSelected(boolean value) {
		viewOptions.setIsIncludeFolders(value);
	}

	@Override
	public boolean isTableSelected() {
		return viewOptions.isIncludeTables();
	}

	@Override
	public void setIsTableSelected(boolean value) {
		viewOptions.setIsIncludeTables(value);
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
	public void setViewTypeOptionsVisible(boolean visible) {
		viewOptionsContainer.setVisible(visible);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		p = presenter;
	}
}
