package org.sagebionetworks.web.client.widget.team.controller;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Image;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.SynapseJSNIUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamEditModalWidgetViewImpl implements IsWidget, TeamEditModalWidgetView {

	@UiField
	TextBox editNameField;
	
	@UiField
	TextArea editDescriptionField;
	
	@UiField
	CheckBox publicJoinCheckbox;
	
	@UiField
	Button primaryButton;
	
	@UiField
	Button secondaryButton;
	
	@UiField
	Modal modal;
	
	@UiField
	SimplePanel synAlertPanel;
	
	@UiField
	SimplePanel uploadWidgetPanel;
	
	@UiField
	Icon defaultIcon;
	
	@UiField
	Image previewImage;
	
	@UiField
	Div teamImageLoading;
	
	@UiField
	Div iconContainer;

	public interface Binder extends UiBinder<Widget, TeamEditModalWidgetViewImpl> {}
	
	Widget widget;
	Presenter presenter;
	Team team;
	
	@Inject
	public TeamEditModalWidgetViewImpl(Binder uiBinder) {
		this.widget = uiBinder.createAndBindUi(this);
		primaryButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onConfirm(editNameField.getValue(), editDescriptionField.getValue(),
						publicJoinCheckbox.getValue());
			}
		});
		secondaryButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.clear();
				modal.hide();
			}
		});
		KeyDownHandler saveInfo = new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					primaryButton.click();
				}
			}
		};
		editNameField.addKeyDownHandler(saveInfo);
		editDescriptionField.addKeyDownHandler(saveInfo);
	}
	
	@Override
	public void setTeam(Team team) {
		this.team = team;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setAlertWidget(Widget synAlert) {
		synAlertPanel.setWidget(synAlert);
	}
	
	@Override
	public void setUploadWidget(Widget uploader) {
		uploadWidgetPanel.setWidget(uploader);
	}
	
	@Override
	public void setLoading(boolean isLoading) {
		String primaryButtonText = isLoading ? "Uploading" : "Save";
		primaryButton.setText(primaryButtonText);
		primaryButton.setEnabled(!isLoading);
		iconContainer.setVisible(!isLoading);
		teamImageLoading.setVisible(isLoading);
	}
	
	@Override
	public String getName() {
		return editNameField.getValue();
	}
	
	@Override
	public String getDescription() {
		return editDescriptionField.getValue();
	}
	
	@Override
	public boolean getPublicJoin() {
		return publicJoinCheckbox.getValue();
	}

	@Override
	public void setVisible(boolean isVisible) {
		if (isVisible) {
			presenter.clear();
			setLoading(false);
			editNameField.setValue(team.getName());
			editDescriptionField.setValue(team.getDescription());
			publicJoinCheckbox.setValue(team.getCanPublicJoin());
			this.modal.show();
		} else {
			this.modal.hide();
		}
	}
	
	@Override
	public void setImageURL(String url) {
		defaultIcon.setVisible(false);
		previewImage.setVisible(true);
		teamImageLoading.setVisible(false);
		previewImage.setUrl(url);
	}	
	
	@Override
	public void setDefaultIconVisible() {
		defaultIcon.setVisible(true);
		previewImage.setVisible(false);
		teamImageLoading.setVisible(false);
	}
}
