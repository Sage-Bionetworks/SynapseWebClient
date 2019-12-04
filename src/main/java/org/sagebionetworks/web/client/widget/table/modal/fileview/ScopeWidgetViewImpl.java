package org.sagebionetworks.web.client.widget.table.modal.fileview;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Div;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ScopeWidgetViewImpl implements ScopeWidgetView {

	public interface Binder extends UiBinder<Widget, ScopeWidgetViewImpl> {
	}

	@UiField
	SimplePanel viewScopeContainer;
	@UiField
	SimplePanel editScopeContainer;
	@UiField
	SimplePanel editScopeAlertContainer;
	@UiField
	Button saveButton;
	@UiField
	Button editButton;
	@UiField
	Modal editModal;
	@UiField
	Div viewOptionsContainer;
	Widget widget;
	Presenter presenter;
	FileViewOptions viewOptions;

	@Inject
	public ScopeWidgetViewImpl(Binder binder, FileViewOptions viewOptions) {
		widget = binder.createAndBindUi(this);
		this.viewOptions = viewOptions;
		viewOptionsContainer.add(viewOptions);
		editButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onEdit();
			}
		});
		saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSave();
			}
		});
		viewOptions.addClickHandler(event -> {
			presenter.updateViewTypeMask();
		});
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setVisible(boolean visible) {
		widget.setVisible(visible);
	}

	@Override
	public void setEntityListWidget(IsWidget entityListWidget) {
		viewScopeContainer.clear();
		viewScopeContainer.setWidget(entityListWidget);
	}

	@Override
	public void setEditableEntityListWidget(IsWidget entityListWidget) {
		editScopeContainer.clear();
		editScopeContainer.setWidget(entityListWidget);
	}

	@Override
	public void showModal() {
		editModal.show();
	}

	@Override
	public void hideModal() {
		editModal.hide();
	}

	@Override
	public void setSynAlert(IsWidget w) {
		editScopeAlertContainer.clear();
		editScopeAlertContainer.setWidget(w);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setEditButtonVisible(boolean visible) {
		editButton.setVisible(visible);
	}

	@Override
	public void setLoading(boolean loading) {
		if (!loading) {
			this.saveButton.state().reset();
		} else {
			this.saveButton.state().loading();
		}
	}

	@Override
	public void setViewTypeOptionsVisible(boolean visible) {
		viewOptionsContainer.setVisible(visible);
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

}
