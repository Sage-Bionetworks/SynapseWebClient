package org.sagebionetworks.web.client.widget.entity.controller;

import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.file.UploadType;
import org.sagebionetworks.repo.model.project.ExternalS3StorageLocationSetting;
import org.sagebionetworks.repo.model.project.ExternalStorageLocationSetting;
import org.sagebionetworks.repo.model.project.StorageLocationSetting;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
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
		getMyLocationSettingBanners();
	}
	
	public void getMyLocationSettingBanners() {
		synapseClient.getMyLocationSettingBanners(new AsyncCallback<List<String>>() {
			@Override
			public void onFailure(Throwable caught) {
				hide();
				view.showErrorMessage(caught.getMessage());
			}
			public void onSuccess(List<String> banners) {
				view.setBannerDropdownVisible(!banners.isEmpty());
				view.setBannerSuggestions(banners);
				getStorageLocationSetting();
			};
		});
	}
	
	public void getStorageLocationSetting() {
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
						view.setBaseKey(setting.getBaseKey().trim());
						view.setBucket(setting.getBucket().trim());
						view.setExternalS3Banner(setting.getBanner().trim());
						view.selectExternalS3Storage();
					} else if (location instanceof ExternalStorageLocationSetting) {
						ExternalStorageLocationSetting setting= (ExternalStorageLocationSetting) location;
						view.setSFTPUrl(setting.getUrl().trim());
						view.setSFTPBanner(setting.getBanner().trim());
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
		synAlert.clear();
		StorageLocationSetting setting = getStorageLocationSettingFromView();
		String error = validate(setting);
		if (error != null) {
			synAlert.showError(error);
		} else {
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
			synapseClient.createStorageLocationSetting(entityBundle.getEntity().getId(), setting, callback);	
		}
	}
	
	public StorageLocationSetting getStorageLocationSettingFromView() {
		if (view.isExternalS3StorageSelected()) {
			ExternalS3StorageLocationSetting setting = new ExternalS3StorageLocationSetting();
			setting.setBanner(view.getExternalS3Banner().trim());
			setting.setBucket(view.getBucket().trim());
			setting.setBaseKey(view.getBaseKey().trim());
			setting.setUploadType(UploadType.S3);
			return setting;
		} else if (view.isSFTPStorageSelected()) {
			ExternalStorageLocationSetting setting = new ExternalStorageLocationSetting();
			setting.setUrl(view.getSFTPUrl().trim());
			setting.setBanner(view.getSFTPBanner().trim());
			setting.setUploadType(UploadType.SFTP);
			return setting;
		} else {
			//default synapse storage
			return null;
		}
	}
	
	/**
	 * Up front validation of storage setting parameters.
	 * @param setting
	 * @return Returns an error string if problems are detected with the input, null otherwise.  Note, returns null if settings object is null (default synapse storage).  
	 */
	public String validate(StorageLocationSetting setting) {
		if (setting != null) {
			if (setting instanceof ExternalS3StorageLocationSetting) {
				ExternalS3StorageLocationSetting externalS3StorageLocationSetting = (ExternalS3StorageLocationSetting)setting;
				if (externalS3StorageLocationSetting.getBucket().isEmpty()) {
					return "Bucket is required.";
				}
			} else if (setting instanceof ExternalStorageLocationSetting) {
				ExternalStorageLocationSetting externalStorageLocationSetting = (ExternalStorageLocationSetting) setting;
				if (!isValidSftpUrl(externalStorageLocationSetting.getUrl())) {
					return "A valid SFTP URL is required.";
				}
			}
		}
		return null;
	}


	public static boolean isValidSftpUrl(String url) {
		if (url == null || url.trim().length() == 0) {
			//url is undefined
			return false;
		}
		RegExp regEx = RegExp.compile(WebConstants.VALID_SFTP_URL_REGEX, "gmi");
		MatchResult matchResult = regEx.exec(url);
		return (matchResult != null && url.equals(matchResult.getGroup(0))); 
	}
	public void setEntityUpdatedHandler(EntityUpdatedHandler updatedHandler) {
		this.entityUpdatedHandler = updatedHandler;
	}
}
