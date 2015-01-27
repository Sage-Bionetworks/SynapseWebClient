package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RegisterTeamDialogViewImpl implements RegisterTeamDialogView {
	private Presenter presenter;
	public interface RegisterTeamDialogViewImplUiBinder extends UiBinder<Widget, RegisterTeamDialogViewImpl> {}
	@UiField
	TextBox recruitmentMessageField;
	@UiField
	Select teamComboBox;
	@UiField
	FormGroup teamSelectionUI;
	@UiField
	Button okButton;
	@UiField
	Button unregisterButton;
	
	Modal modal;
	
	@Inject
	public RegisterTeamDialogViewImpl(RegisterTeamDialogViewImplUiBinder binder) {
		modal = (Modal)binder.createAndBindUi(this);
		teamComboBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				presenter.teamSelected(teamComboBox.getValue());
			}
		});
		
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
	public void showTeamSelector(boolean isVisible) {
		teamSelectionUI.setVisible(isVisible);
	}
	@Override
	public void showUnregisterButton(boolean isVisible) {
		unregisterButton.setVisible(isVisible);
	}
	@Override
	public void clearTeams() {
		teamComboBox.clear();
	}
	
	@Override
	public void setTeams(List<Team> teams) {
		for (Team team : teams) {
			Option teamOption = new Option();
			teamOption.setText(team.getName());
			teamComboBox.add(teamOption);
		}
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
	
	/*
	 * Private Methods
	 */

}
