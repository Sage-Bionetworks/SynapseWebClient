package org.sagebionetworks.web.client.widget.entity.controller;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.file.UploadDestinationLocation;
import org.sagebionetworks.repo.model.project.ExternalS3StorageLocationSetting;
import org.sagebionetworks.repo.model.project.ExternalStorageLocationSetting;
import org.sagebionetworks.repo.model.project.StorageLocationSetting;
import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.provenance.Used;
import org.sagebionetworks.repo.model.provenance.UsedEntity;
import org.sagebionetworks.repo.model.provenance.UsedURL;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;

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
				synAlert.handleException(caught);
			}
			
			@Override
			public void onSuccess(StorageLocationSetting location) {
				//if null, then still show the default UI
				if (location != null) {
					
				}
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void show() {
		clear();
		view.show();
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
			return setting;
		} else if (view.isSFTPStorageSelected()) {
			ExternalStorageLocationSetting setting = new ExternalStorageLocationSetting();
			setting.setUrl(view.getSFTPUrl());
			setting.setBanner(view.getSFTPBanner());
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
