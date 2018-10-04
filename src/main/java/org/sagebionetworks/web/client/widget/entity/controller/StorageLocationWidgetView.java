package org.sagebionetworks.web.client.widget.entity.controller;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

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
	void selectExternalObjectStore();
	boolean isExternalObjectStoreSelected();
	void setExternalObjectStoreBanner(String banner);
	String getExternalObjectStoreBanner();
	void setExternalObjectStoreBucket(String bucket);
	String getExternalObjectStoreBucket();
	void setExternalObjectStoreEndpointUrl(String url);
	String getExternalObjectStoreEndpointUrl();
	void setExternalObjectStoreVisible(boolean visible);
	String getBucket();
	void setBucket(String bucket);
	String getBaseKey();
	void setBaseKey(String baseKey);
	String getExternalS3Banner();
	void setExternalS3Banner(String banner);
	void selectSFTPStorage();
	boolean isSFTPStorageSelected();
	String getSFTPUrl();
	void setSFTPUrl(String url);
	String getSFTPBanner();
	void setSFTPBanner(String banner);
	void showErrorMessage(String message);
	void setBannerSuggestions(List<String> banners);
	void setBannerDropdownVisible(boolean isVisible);
	void setSFTPVisible(boolean visible);
	void setLoading(boolean isLoading);
}
