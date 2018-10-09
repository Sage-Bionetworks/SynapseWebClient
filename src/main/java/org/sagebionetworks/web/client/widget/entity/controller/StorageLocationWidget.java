package org.sagebionetworks.web.client.widget.entity.controller;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.file.UploadType;
import org.sagebionetworks.repo.model.project.ExternalObjectStorageLocationSetting;
import org.sagebionetworks.repo.model.project.ExternalS3StorageLocationSetting;
import org.sagebionetworks.repo.model.project.ExternalStorageLocationSetting;
import org.sagebionetworks.repo.model.project.StorageLocationSetting;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class StorageLocationWidget implements StorageLocationWidgetView.Presenter {

	StorageLocationWidgetView view;
	SynapseClientAsync synapseClient;
	SynapseAlert synAlert;
	EntityBundle entityBundle;
	CookieProvider cookies;
	EventBus eventBus;
	
	@Inject
	public StorageLocationWidget(StorageLocationWidgetView view,
			SynapseClientAsync synapseClient, 
			SynapseAlert synAlert, 
			CookieProvider cookies,
			EventBus eventBus) {
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.synAlert = synAlert;
		this.cookies = cookies;
		this.eventBus = eventBus;
		view.setSynAlertWidget(synAlert);
		view.setPresenter(this);
	}
	
	public void configure(EntityBundle entityBundle) {
		this.entityBundle = entityBundle;
		clear();
		view.setLoading(true);
		getStorageLocationSetting();
		getMyLocationSettingBanners();
		boolean isInAlpha = DisplayUtils.isInTestWebsite(cookies);
		view.setSFTPVisible(isInAlpha);
		view.setExternalObjectStoreVisible(isInAlpha);
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
			};
		});
	}
	
	public void getStorageLocationSetting() {
		Entity entity = entityBundle.getEntity();
		view.setSFTPVisible(DisplayUtils.isInTestWebsite(cookies));
		synapseClient.getStorageLocationSetting(entity.getId(), new AsyncCallback<StorageLocationSetting>() {
			@Override
			public void onFailure(Throwable caught) {
				// unable to get storage location
				// if this is a proxy, then upload is not supported.  Let the user set back to default Synapse.
				view.showErrorMessage(caught.getMessage());
				view.setLoading(false);
			}
			
			@Override
			public void onSuccess(StorageLocationSetting location) {
				//if null, then still show the default UI
				if (location != null) {
					//set up the view
					String banner = location.getBanner() != null ? location.getBanner().trim() : "";
					if (location instanceof ExternalS3StorageLocationSetting) {
						ExternalS3StorageLocationSetting setting = (ExternalS3StorageLocationSetting) location;
						view.setBaseKey(setting.getBaseKey().trim());
						view.setBucket(setting.getBucket().trim());
						view.setExternalS3Banner(banner);
						view.selectExternalS3Storage();
					} else if (location instanceof ExternalObjectStorageLocationSetting) {
						ExternalObjectStorageLocationSetting setting = (ExternalObjectStorageLocationSetting) location;
						view.setExternalObjectStoreBanner(banner);
						view.setExternalObjectStoreBucket(setting.getBucket().trim());
						view.setExternalObjectStoreEndpointUrl(setting.getEndpointUrl().trim());
						view.selectExternalObjectStore();
					} else if (location instanceof ExternalStorageLocationSetting) {
						view.setSFTPVisible(true);
						ExternalStorageLocationSetting setting= (ExternalStorageLocationSetting) location;
						view.setSFTPUrl(setting.getUrl().trim());
						view.setSFTPBanner(banner);
						view.selectSFTPStorage();
					}
				}
				view.setLoading(false);
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
					eventBus.fireEvent(new EntityUpdatedEvent());
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
		} else if (view.isExternalObjectStoreSelected()) {
			ExternalObjectStorageLocationSetting setting = new ExternalObjectStorageLocationSetting();
			setting.setBanner(view.getExternalObjectStoreBanner().trim());
			setting.setBucket(view.getExternalObjectStoreBucket().trim());
			setting.setEndpointUrl(view.getExternalObjectStoreEndpointUrl().trim());
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
				if (externalS3StorageLocationSetting.getBucket().trim().isEmpty()) {
					return "Bucket is required.";
				}
			} else if (setting instanceof ExternalStorageLocationSetting) {
				ExternalStorageLocationSetting externalStorageLocationSetting = (ExternalStorageLocationSetting) setting;
				if (!isValidSftpUrl(externalStorageLocationSetting.getUrl().trim())) {
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
}
