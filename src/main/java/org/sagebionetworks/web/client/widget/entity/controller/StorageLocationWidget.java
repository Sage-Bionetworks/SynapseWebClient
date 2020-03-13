package org.sagebionetworks.web.client.widget.entity.controller;

import static org.sagebionetworks.web.client.DisplayUtils.*;
import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.List;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.file.UploadType;
import org.sagebionetworks.repo.model.project.ExternalGoogleCloudStorageLocationSetting;
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
	public StorageLocationWidget(StorageLocationWidgetView view, SynapseClientAsync synapseClient, SynapseAlert synAlert, CookieProvider cookies, EventBus eventBus) {
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
				// if this is a proxy, then upload is not supported. Let the user set back to default Synapse.
				view.showErrorMessage(caught.getMessage());
				view.setLoading(false);
			}

			@Override
			public void onSuccess(StorageLocationSetting location) {
				// if null, then still show the default UI
				if (location != null) {
					// set up the view
					String banner = trim(location.getBanner());
					if (location instanceof ExternalS3StorageLocationSetting) {
						ExternalS3StorageLocationSetting setting = (ExternalS3StorageLocationSetting) location;
						view.setS3BaseKey(trim(setting.getBaseKey()));
						view.setS3Bucket(trim(setting.getBucket()));
						view.setExternalS3Banner(banner);
						view.selectExternalS3Storage();
					} else if (location instanceof ExternalGoogleCloudStorageLocationSetting) {
						view.setGoogleCloudVisible(true);
						ExternalGoogleCloudStorageLocationSetting setting = (ExternalGoogleCloudStorageLocationSetting) location;
						view.setGoogleCloudBaseKey(trim(setting.getBaseKey()));
						view.setGoogleCloudBucket(trim(setting.getBucket()));
						view.setExternalGoogleCloudBanner(banner);
						view.selectExternalGoogleCloudStorage();
					} else if (location instanceof ExternalObjectStorageLocationSetting) {
						ExternalObjectStorageLocationSetting setting = (ExternalObjectStorageLocationSetting) location;
						view.setExternalObjectStoreBanner(banner);
						view.setExternalObjectStoreBucket(trim(setting.getBucket()));
						view.setExternalObjectStoreEndpointUrl(trim(setting.getEndpointUrl()));
						view.selectExternalObjectStore();
					} else if (location instanceof ExternalStorageLocationSetting) {
						view.setSFTPVisible(true);
						ExternalStorageLocationSetting setting = (ExternalStorageLocationSetting) location;
						view.setSFTPUrl(trim(setting.getUrl()));
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
		String error = null;
		StorageLocationSetting setting = null;
		try {
			setting = getStorageLocationSettingFromView();
			error = validate(setting);
		} catch (Exception e) {
			error = e.getMessage();
		}
		if (error != null) {
			synAlert.showError(error);
		} else {
			// look for duplicate storage location in existing settings
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

	/**
	 * Note that 
	 * @return
	 * @throws IllegalArgumentException
	 */
	public StorageLocationSetting getStorageLocationSettingFromView() throws Exception {
		try {
			if (view.isExternalS3StorageSelected()) {
				ExternalS3StorageLocationSetting setting = new ExternalS3StorageLocationSetting();
				setting.setBanner(replaceWithNullIfEmptyTrimmedString(view.getExternalS3Banner()));
				setting.setBucket(replaceWithNullIfEmptyTrimmedString(view.getS3Bucket()));
				setting.setBaseKey(replaceWithNullIfEmptyTrimmedString(view.getS3BaseKey()));
				setting.setUploadType(UploadType.S3);
				return setting;
			} else if (view.isExternalGoogleCloudStorageSelected()) {
				ExternalGoogleCloudStorageLocationSetting setting = new ExternalGoogleCloudStorageLocationSetting();
				setting.setBanner(replaceWithNullIfEmptyTrimmedString(view.getExternalGoogleCloudBanner()));
				setting.setBucket(replaceWithNullIfEmptyTrimmedString(view.getGoogleCloudBucket()));
				setting.setBaseKey(replaceWithNullIfEmptyTrimmedString(view.getGoogleCloudBaseKey()));
				setting.setUploadType(UploadType.GOOGLECLOUDSTORAGE);
				return setting;
			} else if (view.isExternalObjectStoreSelected()) {
				ExternalObjectStorageLocationSetting setting = new ExternalObjectStorageLocationSetting();
				setting.setBanner(replaceWithNullIfEmptyTrimmedString(view.getExternalObjectStoreBanner()));
				setting.setBucket(replaceWithNullIfEmptyTrimmedString(view.getExternalObjectStoreBucket()));
				setting.setEndpointUrl(replaceWithNullIfEmptyTrimmedString(view.getExternalObjectStoreEndpointUrl()));
				setting.setUploadType(UploadType.S3);
				return setting;
			} else if (view.isSFTPStorageSelected()) {
				ExternalStorageLocationSetting setting = new ExternalStorageLocationSetting();
				setting.setUrl(replaceWithNullIfEmptyTrimmedString(view.getSFTPUrl()));
				setting.setBanner(replaceWithNullIfEmptyTrimmedString(view.getSFTPBanner()));
				setting.setUploadType(UploadType.SFTP);
				return setting;
			} else {
				// default synapse storage
				return null;
			}
		} catch (RuntimeException e) {
			// The storage location settings now throw an illegal argument exception if you attempt to instantiate with invalid params
			// We would throw an InstantiationException, but it is not emulated by GWT.
			 throw new Exception(e.getMessage());
		}
	}

	/**
	 * Up front validation of storage setting parameters.
	 * 
	 * @param setting
	 * @return Returns an error string if problems are detected with the input, null otherwise. Note,
	 *         returns null if settings object is null (default synapse storage).
	 */
	public String validate(StorageLocationSetting setting) {
		if (setting != null) {
			if (setting instanceof ExternalS3StorageLocationSetting) {
				ExternalS3StorageLocationSetting externalS3StorageLocationSetting = (ExternalS3StorageLocationSetting) setting;
				if (externalS3StorageLocationSetting.getBucket() == null || externalS3StorageLocationSetting.getBucket().trim().isEmpty()) {
					return "Bucket is required.";
				}
			} else if (setting instanceof ExternalGoogleCloudStorageLocationSetting) {
				ExternalGoogleCloudStorageLocationSetting externalGoogleCloudStorageLocationSetting = (ExternalGoogleCloudStorageLocationSetting) setting;
				if (externalGoogleCloudStorageLocationSetting.getBucket() == null || externalGoogleCloudStorageLocationSetting.getBucket().trim().isEmpty()) {
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
			// url is undefined
			return false;
		}
		RegExp regEx = RegExp.compile(WebConstants.VALID_SFTP_URL_REGEX, "gmi");
		MatchResult matchResult = regEx.exec(url);
		return (matchResult != null && url.equals(matchResult.getGroup(0)));
	}
}
