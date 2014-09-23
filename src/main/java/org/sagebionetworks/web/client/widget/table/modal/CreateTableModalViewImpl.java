package org.sagebionetworks.web.client.widget.table.modal;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A basic implementation of  CreateTableModalView with zero business logic.
 * 
 * @author jhill
 *
 */
public class CreateTableModalViewImpl implements CreateTableModalView {
	
	public interface Binder extends UiBinder<Modal, CreateTableModalViewImpl> {}
	
	@UiField
	TextBox tableNameField;
	@UiField
	Alert alert;
	@UiField
	Button createButton;
	
	Modal createTableModal;

	@Inject
	public CreateTableModalViewImpl(Binder binder){
		createTableModal = binder.createAndBindUi(this);
	}
	
	@Override
	public Widget asWidget() {
		return createTableModal;
	}

	@Override
	public String getTableName() {
		return tableNameField.getText();
	}

	@Override
	public void showError(String error) {
		alert.setVisible(true);
		alert.setText(error);
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.createButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				presenter.onCreateTable();
			}
		});
	}

	@Override
	public void hide() {
		createTableModal.hide();
	}

	@Override
	public void show() {
		createTableModal.show();
	}

	@Override
	public void clear() {
		this.createButton.state().reset();
		this.alert.setVisible(false);
		this.tableNameField.clear();
	}

	@Override
	public void setLoading(boolean isLoading) {
		if(isLoading){
			this.createButton.state().loading();
		}else{
			this.createButton.state().reset();
		}	
	}

}
