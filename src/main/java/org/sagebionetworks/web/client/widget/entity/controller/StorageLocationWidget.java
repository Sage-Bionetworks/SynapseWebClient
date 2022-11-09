package org.sagebionetworks.web.client.widget.entity.controller;

import static org.sagebionetworks.web.client.DisplayUtils.replaceWithNullIfEmptyTrimmedString;
import static org.sagebionetworks.web.client.DisplayUtils.trim;
import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.List;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.file.ExternalGoogleCloudUploadDestination;
import org.sagebionetworks.repo.model.file.ExternalObjectStoreUploadDestination;
import org.sagebionetworks.repo.model.file.ExternalS3UploadDestination;
import org.sagebionetworks.repo.model.file.UploadDestination;
import org.sagebionetworks.repo.model.file.UploadType;
import org.sagebionetworks.repo.model.project.ExternalGoogleCloudStorageLocationSetting;
import org.sagebionetworks.repo.model.project.ExternalObjectStorageLocationSetting;
import org.sagebionetworks.repo.model.project.ExternalS3StorageLocationSetting;
import org.sagebionetworks.repo.model.project.StorageLocationSetting;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.shared.WebConstants;

public class StorageLocationWidget
  implements StorageLocationWidgetView.Presenter {

  StorageLocationWidgetView view;
  SynapseClientAsync synapseClient;
  SynapseJavascriptClient jsClient;
  SynapseAlert synAlert;
  EntityBundle entityBundle;
  CookieProvider cookies;
  EventBus eventBus;
  SynapseProperties synapseProperties;

  @Inject
  public StorageLocationWidget(
    StorageLocationWidgetView view,
    SynapseClientAsync synapseClient,
    SynapseJavascriptClient jsClient,
    SynapseAlert synAlert,
    SynapseProperties synapseProperties,
    CookieProvider cookies,
    EventBus eventBus
  ) {
    this.view = view;
    this.synapseClient = synapseClient;
    fixServiceEntryPoint(synapseClient);
    this.jsClient = jsClient;
    this.synAlert = synAlert;
    this.cookies = cookies;
    this.synapseProperties = synapseProperties;
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
    view.setExternalObjectStoreVisible(isInAlpha);
  }

  public void getMyLocationSettingBanners() {
    synapseClient.getMyLocationSettingBanners(
      new AsyncCallback<List<String>>() {
        @Override
        public void onFailure(Throwable caught) {
          hide();
          view.showErrorMessage(caught.getMessage());
        }

        public void onSuccess(List<String> banners) {
          view.setBannerDropdownVisible(!banners.isEmpty());
          view.setBannerSuggestions(banners);
        }
      }
    );
  }

  public void getStorageLocationSetting() {
    Entity entity = entityBundle.getEntity();
    jsClient.getDefaultUploadDestination(
      entity.getId(),
      new AsyncCallback<UploadDestination>() {
        @Override
        public void onFailure(Throwable caught) {
          // unable to get storage location
          // if this is a proxy, then upload is not supported. Let the user set back to default Synapse.
          view.showErrorMessage(caught.getMessage());
          view.setLoading(false);
        }

        @Override
        public void onSuccess(UploadDestination uploadDestination) {
          // if null, then still show the default UI
          boolean isInAlpha = DisplayUtils.isInTestWebsite(cookies);
          view.setS3StsVisible(isInAlpha);
          Long defaultStorageId = Long.parseLong(
            synapseProperties.getSynapseProperty(
              WebConstants.DEFAULT_STORAGE_ID_PROPERTY_KEY
            )
          );
          if (
            uploadDestination != null &&
            !defaultStorageId.equals(uploadDestination.getStorageLocationId())
          ) {
            // set up the view
            String banner = trim(uploadDestination.getBanner());
            if (uploadDestination instanceof ExternalS3UploadDestination) {
              ExternalS3UploadDestination destination = (ExternalS3UploadDestination) uploadDestination;
              boolean isStsEnabled = destination.getStsEnabled() == null
                ? false
                : destination.getStsEnabled();
              view.setS3BaseKey(trim(destination.getBaseKey()));
              view.setS3Bucket(trim(destination.getBucket()));
              if (isStsEnabled) view.setS3StsVisible(true);
              view.setS3StsEnabled(isStsEnabled);
              view.setExternalS3Banner(banner);
              view.selectExternalS3Storage();
            } else if (
              uploadDestination instanceof ExternalGoogleCloudUploadDestination
            ) {
              view.setGoogleCloudVisible(true);
              ExternalGoogleCloudUploadDestination destination = (ExternalGoogleCloudUploadDestination) uploadDestination;
              view.setGoogleCloudBaseKey(trim(destination.getBaseKey()));
              view.setGoogleCloudBucket(trim(destination.getBucket()));
              view.setExternalGoogleCloudBanner(banner);
              view.selectExternalGoogleCloudStorage();
            } else if (
              uploadDestination instanceof ExternalObjectStoreUploadDestination
            ) {
              ExternalObjectStoreUploadDestination destination = (ExternalObjectStoreUploadDestination) uploadDestination;
              view.setExternalObjectStoreBanner(banner);
              view.setExternalObjectStoreBucket(trim(destination.getBucket()));
              view.setExternalObjectStoreEndpointUrl(
                trim(destination.getEndpointUrl())
              );
              view.selectExternalObjectStore();
            }
          }
          view.setLoading(false);
        }
      }
    );
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
          eventBus.fireEvent(
            new EntityUpdatedEvent(entityBundle.getEntity().getId())
          );
        }
      };
      synapseClient.createStorageLocationSetting(
        entityBundle.getEntity().getId(),
        setting,
        callback
      );
    }
  }

  /**
   * Note that
   * @return
   * @throws IllegalArgumentException
   */
  public StorageLocationSetting getStorageLocationSettingFromView()
    throws Exception {
    try {
      if (view.isExternalS3StorageSelected()) {
        ExternalS3StorageLocationSetting setting = new ExternalS3StorageLocationSetting();
        setting.setBanner(
          replaceWithNullIfEmptyTrimmedString(view.getExternalS3Banner())
        );
        setting.setBucket(
          replaceWithNullIfEmptyTrimmedString(view.getS3Bucket())
        );
        setting.setBaseKey(
          replaceWithNullIfEmptyTrimmedString(view.getS3BaseKey())
        );
        setting.setStsEnabled(view.getS3StsEnabled());
        setting.setUploadType(UploadType.S3);
        return setting;
      } else if (view.isExternalGoogleCloudStorageSelected()) {
        ExternalGoogleCloudStorageLocationSetting setting = new ExternalGoogleCloudStorageLocationSetting();
        setting.setBanner(
          replaceWithNullIfEmptyTrimmedString(
            view.getExternalGoogleCloudBanner()
          )
        );
        setting.setBucket(
          replaceWithNullIfEmptyTrimmedString(view.getGoogleCloudBucket())
        );
        setting.setBaseKey(
          replaceWithNullIfEmptyTrimmedString(view.getGoogleCloudBaseKey())
        );
        setting.setUploadType(UploadType.GOOGLECLOUDSTORAGE);
        return setting;
      } else if (view.isExternalObjectStoreSelected()) {
        ExternalObjectStorageLocationSetting setting = new ExternalObjectStorageLocationSetting();
        setting.setBanner(
          replaceWithNullIfEmptyTrimmedString(
            view.getExternalObjectStoreBanner()
          )
        );
        setting.setBucket(
          replaceWithNullIfEmptyTrimmedString(
            view.getExternalObjectStoreBucket()
          )
        );
        setting.setEndpointUrl(
          replaceWithNullIfEmptyTrimmedString(
            view.getExternalObjectStoreEndpointUrl()
          )
        );
        setting.setUploadType(UploadType.S3);
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
        if (
          externalS3StorageLocationSetting.getBucket() == null ||
          externalS3StorageLocationSetting.getBucket().trim().isEmpty()
        ) {
          return "Bucket is required.";
        }
      } else if (setting instanceof ExternalGoogleCloudStorageLocationSetting) {
        ExternalGoogleCloudStorageLocationSetting externalGoogleCloudStorageLocationSetting = (ExternalGoogleCloudStorageLocationSetting) setting;
        if (
          externalGoogleCloudStorageLocationSetting.getBucket() == null ||
          externalGoogleCloudStorageLocationSetting.getBucket().trim().isEmpty()
        ) {
          return "Bucket is required.";
        }
      }
    }
    return null;
  }
}
