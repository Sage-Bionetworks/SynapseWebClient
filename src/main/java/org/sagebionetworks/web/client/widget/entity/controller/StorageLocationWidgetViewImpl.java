package org.sagebionetworks.web.client.widget.entity.controller;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.Radio;
import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class StorageLocationWidgetViewImpl implements StorageLocationWidgetView {
	
	public interface StorageLocationWidgetViewImplUiBinder 
			extends UiBinder<Widget, StorageLocationWidgetViewImpl> {}
	
	@UiField
	Modal modal;
	
	@UiField
	SimplePanel synAlertPanel;
	
	@UiField
	TextBox bucketField;
	
	@UiField
	TextBox sftpUrlField;
	
	@UiField
	Radio synapseStorageButton;
	@UiField
	Radio externalS3Button;
	@UiField
	Radio sftpButton;
	
	@UiField
	Button saveButton;
	
	@UiField
	Button cancelButton;
	
	Widget widget;
	Presenter presenter;
	
	@Inject
	public StorageLocationWidgetViewImpl(StorageLocationWidgetViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
		saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSave();
			}
		});
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				modal.hide();
			}
		});
		synapseStorageButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				bucketField.setEnabled(false);
				sftpUrlField.setEnabled(false);
			}
		});
		externalS3Button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				bucketField.setEnabled(true);
				sftpUrlField.setEnabled(false);
			}
		});
		sftpButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				bucketField.setEnabled(false);
				sftpUrlField.setEnabled(true);
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setSynAlertWidget(IsWidget synAlert) {
		synAlertPanel.setWidget(synAlert);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void clear() {
		bucketField.setText("");
		sftpUrlField.setText("");
	}

	@Override
	public void hide() {
		modal.hide();
	}

	@Override
	public void show() {
		modal.show();
	}

	@Override
	public void selectSynapseStorage() {
		synapseStorageButton.setValue(true, true);
	}

	@Override
	public boolean isSynapseStorageSelected() {
		return synapseStorageButton.getValue();
	}

	@Override
	public void selectExternalS3Storage() {
		externalS3Button.setValue(true, true);
	}

	@Override
	public boolean isExternalS3StorageSelected() {
		return externalS3Button.getValue();
	}

	@Override
	public String getBucket() {
		return bucketField.getValue();
	}

	@Override
	public void selectSFTPStorage() {
		sftpButton.setValue(true, true);
	}

	@Override
	public boolean isSFTPStorageSelected() {
		return sftpButton.getValue();
	}

	@Override
	public String getSFTPUrl() {
		return sftpUrlField.getValue();
	}
}
