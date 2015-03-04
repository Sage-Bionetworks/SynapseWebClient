package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EditRegisteredTeamDialogViewImpl implements EditRegisteredTeamDialogView {
	private Presenter presenter;
	public interface RegisterTeamDialogViewImplUiBinder extends UiBinder<Widget, EditRegisteredTeamDialogViewImpl> {}
	@UiField
	TextBox recruitmentMessageField;
	@UiField
	Button okButton;
	@UiField
	Button unregisterButton;
	
	Modal modal;
	
	@Inject
	public EditRegisteredTeamDialogViewImpl(RegisterTeamDialogViewImplUiBinder binder) {
		modal = (Modal)binder.createAndBindUi(this);
		
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onOk();
			}
		});
		
		unregisterButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onUnregister();
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return modal;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void setRecruitmentMessage(String message) {
		recruitmentMessageField.setValue(message);	
	}
	@Override
	public String getRecruitmentMessage() {
		return recruitmentMessageField.getValue();
	}
	
	@Override
	public void showModal() {
		modal.show();
	}
	
	@Override
	public void hideModal() {
		modal.hide();
	}
	
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
	/*
	 * Private Methods
	 */

}
