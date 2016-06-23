package org.sagebionetworks.web.client.widget.table.modal.fileview;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ScopeWidgetViewImpl implements ScopeWidgetView {

	public interface Binder extends UiBinder<Widget, ScopeWidgetViewImpl> {}
	
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
	Widget widget;
	Presenter presenter;
	
	@Inject
	public ScopeWidgetViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
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
	
}
