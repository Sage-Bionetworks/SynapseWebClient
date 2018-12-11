package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.gwtbootstrap3.extras.bootbox.client.callback.ConfirmCallback;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ListBox;
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
	Button cancelButton;
	@UiField
	ListBox teamComboBox;
	@UiField
	Anchor createNewTeamLink;
	@UiField
	Paragraph noTeamsFoundUI;
	@UiField
	Div teamSelectComboUI;
	@UiField
	Button refreshTeamsButton;
	
	Modal modal;
	@Inject
	public RegisterTeamDialogViewImpl(RegisterTeamDialogViewImplUiBinder binder) {
		modal = (Modal)binder.createAndBindUi(this);
		recruitmentMessageField.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					okButton.click();
				}
			}
		});
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onOk();
			}
		});
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				modal.hide();
			}
		});
		teamComboBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				presenter.teamSelected(teamComboBox.getSelectedIndex());
			}
		});
		refreshTeamsButton.addClickHandler(event -> {
			presenter.refreshRegistratableTeams();
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
		teamComboBox.clear();
		for (Team team : teams) {
			teamComboBox.addItem(team.getName());
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
	
	@Override
	public void setNoTeamsFoundVisible(boolean isVisible) {
		noTeamsFoundUI.setVisible(isVisible);
		teamSelectComboUI.setVisible(!isVisible);
	}
	
	@Override
	public void showConfirmDialog(String message, Callback callback) {
		DisplayUtils.confirm(message, callback);
	}
	
	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void setNewTeamLink(String url) {
		createNewTeamLink.setHref(url);
	}

	/*
	 * Private Methods
	 */

}
