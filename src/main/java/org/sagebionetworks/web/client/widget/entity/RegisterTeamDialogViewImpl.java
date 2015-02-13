package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Span;
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
	FormGroup teamSelectionUI;
	@UiField
	Button okButton;
	@UiField
	Span teamComboBoxContainer;
	
	Modal modal;
	Select teamComboBox;
	
	@Inject
	public RegisterTeamDialogViewImpl(RegisterTeamDialogViewImplUiBinder binder) {
		modal = (Modal)binder.createAndBindUi(this);
		
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onOk();
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
	public void setTeams(List<Team> teams) {
		teamComboBoxContainer.clear();
		
		teamComboBox = new Select();
		teamComboBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				presenter.teamSelected(teamComboBox.getValue());
			}
		});
		
		for (Team team : teams) {
			Option teamOption = new Option();
			teamOption.setText(team.getName());
			teamComboBox.add(teamOption);
		}
		teamComboBoxContainer.add(teamComboBox);
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
