package org.sagebionetworks.web.client.widget.entity.controller;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import java.util.List;

public interface StorageLocationWidgetView {
  public interface Presenter {
    Widget asWidget();

    void onSave();

    void clear();
  }

  Widget asWidget();

  void setSynAlertWidget(IsWidget asWidget);

  void setPresenter(Presenter presenter);

  void clear();

  void hide();

  void show();

  void selectSynapseStorage();

  boolean isSynapseStorageSelected();

  void selectExternalS3Storage();

  boolean isExternalS3StorageSelected();

  void selectExternalGoogleCloudStorage();

  boolean isExternalGoogleCloudStorageSelected();

  void selectExternalObjectStore();

  boolean isExternalObjectStoreSelected();

  void setExternalObjectStoreBanner(String banner);

  String getExternalObjectStoreBanner();

  void setExternalObjectStoreBucket(String bucket);

  String getExternalObjectStoreBucket();

  void setExternalObjectStoreEndpointUrl(String url);

  String getExternalObjectStoreEndpointUrl();

  void setExternalObjectStoreVisible(boolean visible);

  String getS3Bucket();

  void setS3Bucket(String bucket);

  String getGoogleCloudBucket();

  void setGoogleCloudBucket(String bucket);

  String getS3BaseKey();

  String getGoogleCloudBaseKey();

  void setS3BaseKey(String baseKey);

  void setGoogleCloudBaseKey(String baseKey);

  String getExternalS3Banner();

  void setExternalS3Banner(String banner);

  String getExternalGoogleCloudBanner();

  void setExternalGoogleCloudBanner(String banner);

  void showErrorMessage(String message);

  void setBannerSuggestions(List<String> banners);

  void setBannerDropdownVisible(boolean isVisible);

  void setGoogleCloudVisible(boolean visible);

  void setLoading(boolean isLoading);

  void setS3StsVisible(boolean visible);

  void setS3StsEnabled(boolean stsEnabled);

  boolean getS3StsEnabled();
}
