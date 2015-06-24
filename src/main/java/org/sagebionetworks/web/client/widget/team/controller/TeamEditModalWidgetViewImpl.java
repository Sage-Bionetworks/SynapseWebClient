package org.sagebionetworks.web.client.widget.team.controller;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Image;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ImageType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayUtils;
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
	Span previewImageContainer;
	
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
				presenter.onConfirm();
			}
		});
		secondaryButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
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
	}
	
	@Override
	public void configure(Team team) {
		this.team = team;
		editNameField.setValue(team.getName());
		editDescriptionField.setValue(team.getDescription());
		publicJoinCheckbox.setValue(team.getCanPublicJoin());
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
	public void showLoading() {
		primaryButton.setText("Uploading");
		primaryButton.setEnabled(false);
		iconContainer.setVisible(false);
		teamImageLoading.setVisible(true);
	}
	
	@Override
	public void hideLoading() {
		primaryButton.setText("Save");
		primaryButton.setEnabled(true);
		iconContainer.setVisible(true);
		teamImageLoading.setVisible(false);
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
	public void show() {
		modal.show();
	}
	
	@Override
	public void hide() {
		modal.hide();
	}
	
	@Override
	public void clear() {
		editNameField.setValue("");
		editDescriptionField.setValue("");
		//defaults to the checkbox unchecked, as it's the most common case
		publicJoinCheckbox.setValue(false);
		setDefaultIconVisible();
	}
	
	@Override
	public void setImageURL(String url) {
		defaultIcon.setVisible(false);
		previewImageContainer.setVisible(true);
		teamImageLoading.setVisible(false);
		Image toAdd = new Image(url);
		toAdd.setWidth("150px");
		toAdd.setType(ImageType.ROUNDED);
		previewImageContainer.clear();
		previewImageContainer.add(toAdd);
	}	
	
	@Override
	public void setDefaultIconVisible() {
		defaultIcon.setVisible(true);
		previewImageContainer.setVisible(false);
		teamImageLoading.setVisible(false);
	}
	
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
}
