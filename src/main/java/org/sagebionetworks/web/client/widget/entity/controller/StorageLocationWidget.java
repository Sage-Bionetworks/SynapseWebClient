package org.sagebionetworks.web.client.widget.entity.controller;

import org.gwtbootstrap3.client.shared.event.ShowEvent;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.file.UploadType;
import org.sagebionetworks.repo.model.project.ExternalS3StorageLocationSetting;
import org.sagebionetworks.repo.model.project.ExternalStorageLocationSetting;
import org.sagebionetworks.repo.model.project.StorageLocationSetting;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class StorageLocationWidget implements StorageLocationWidgetView.Presenter {

	StorageLocationWidgetView view;
	SynapseClientAsync synapseClient;
	SynapseAlert synAlert;
	EntityUpdatedHandler entityUpdatedHandler;
	EntityBundle entityBundle;
	
	@Inject
	public StorageLocationWidget(StorageLocationWidgetView view,
			SynapseClientAsync synapseClient, SynapseAlert synAlert) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.synAlert = synAlert;
		view.setSynAlertWidget(synAlert);
		view.setPresenter(this);
	}
	
	@Override
	public void configure(EntityBundle entityBundle, EntityUpdatedHandler entityUpdatedHandler) {
		this.entityBundle = entityBundle;
		this.entityUpdatedHandler = entityUpdatedHandler;
		clear();
		Entity entity = entityBundle.getEntity();
		synapseClient.getStorageLocationSetting(entity.getId(), new AsyncCallback<StorageLocationSetting>() {
			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof ForbiddenException) {
					hide();
					view.showErrorMessage(caught.getMessage());
				} else {
					synAlert.handleException(caught);	
				}
			}
			
			@Override
			public void onSuccess(StorageLocationSetting location) {
				//if null, then still show the default UI
				if (location != null) {
					//set up the view
					if (location instanceof ExternalS3StorageLocationSetting) {
						ExternalS3StorageLocationSetting setting = (ExternalS3StorageLocationSetting) location;
						view.selectExternalS3Storage();
						view.setBaseKey(setting.getBaseKey());
						view.setBucket(setting.getBucket());
						view.setExternalS3Banner(setting.getBanner());
						view.selectExternalS3Storage();
					} else if (location instanceof ExternalStorageLocationSetting) {
						ExternalStorageLocationSetting setting= (ExternalStorageLocationSetting) location;
						//TODO: add basic url validation here
						view.setSFTPUrl(setting.getUrl());
						view.setSFTPBanner(setting.getBanner());
						view.selectSFTPStorage();
					}
				}
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void show() {
		view.show();
		clear();
	}
	
	public void hide() {
		view.hide();
	}
	
	@Override
	public void clear() {
		synAlert.clear();
		view.clear();
	}

	@Override
	public void onSave() {
		//look for duplicate storage location in existing settings
		AsyncCallback<Void> callback = new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(Void result) {
				view.hide();
				entityUpdatedHandler.onPersistSuccess(new EntityUpdatedEvent());
			}
		};
		StorageLocationSetting setting = getStorageLocationSettingFromView();
		
		synapseClient.createStorageLocationSetting(entityBundle.getEntity().getId(), setting, callback);	
	}
	
	public StorageLocationSetting getStorageLocationSettingFromView() {
		if (view.isExternalS3StorageSelected()) {
			ExternalS3StorageLocationSetting setting = new ExternalS3StorageLocationSetting();
			setting.setBanner(view.getExternalS3Banner());
			setting.setBucket(view.getBucket());
			setting.setBaseKey(view.getBaseKey());
			setting.setUploadType(UploadType.S3);
			return setting;
		} else if (view.isSFTPStorageSelected()) {
			ExternalStorageLocationSetting setting = new ExternalStorageLocationSetting();
			setting.setUrl(view.getSFTPUrl());
			setting.setBanner(view.getSFTPBanner());
			setting.setUploadType(UploadType.SFTP);
			return setting;
		} else {
			//default synapse storage
			return null;
		}
	}

	public void setEntityUpdatedHandler(EntityUpdatedHandler updatedHandler) {
		this.entityUpdatedHandler = updatedHandler;
	}
}
