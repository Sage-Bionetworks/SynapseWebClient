package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.shared.event.ModalShownHandler;
import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EditProjectMetadataModalViewImpl implements EditProjectMetadataModalView {

	public interface Binder extends UiBinder<Modal, EditProjectMetadataModalViewImpl> {
	}

	@UiField
	Modal modal;
	@UiField
	TextBox entityNameField;
	@UiField
	TextBox aliasField;
	@UiField
	Alert alert;
	@UiField
	Button primaryButton;
	@UiField
	Div aliasUI;

	@Inject
	public EditProjectMetadataModalViewImpl(Binder binder) {
		binder.createAndBindUi(this);
		modal.addShownHandler(new ModalShownHandler() {

			@Override
			public void onShown(ModalShownEvent evt) {
				entityNameField.setFocus(true);
				entityNameField.selectAll();
			}
		});
	}

	@Override
	public Widget asWidget() {
		return modal;
	}

	@Override
	public String getEntityName() {
		return entityNameField.getText();
	}

	@Override
	public void showError(String error) {
		alert.setVisible(true);
		alert.setText(error);
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.primaryButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				presenter.onPrimary();
			}
		});
		this.entityNameField.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (KeyCodes.KEY_ENTER == event.getNativeKeyCode()) {
					presenter.onPrimary();
				}
			}
		});
	}

	@Override
	public void hide() {
		modal.hide();
	}

	@Override
	public void show() {
		modal.show();
		entityNameField.setFocus(true);
	}

	@Override
	public void clear() {
		this.primaryButton.state().reset();
		this.alert.setVisible(false);
		this.entityNameField.clear();
	}

	@Override
	public void setLoading(boolean isLoading) {
		if (isLoading) {
			this.primaryButton.state().loading();
		} else {
			this.primaryButton.state().reset();
		}
	}

	@Override
	public void configure(String entityName, String alias) {
		this.entityNameField.setText(entityName);
		this.aliasField.setText(alias);
	}

	@Override
	public String getAlias() {
		return aliasField.getText();
	};

	@Override
	public void setAliasUIVisible(boolean visible) {
		aliasUI.setVisible(visible);
	}
}
