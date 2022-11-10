package org.sagebionetworks.web.unitclient.widget.entity.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.Folder;
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
import org.sagebionetworks.web.client.widget.entity.controller.StorageLocationWidget;
import org.sagebionetworks.web.client.widget.entity.controller.StorageLocationWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

@RunWith(MockitoJUnitRunner.class)
public class StorageLocationWidgetTest {

  @Mock
  StorageLocationWidgetView mockView;

  @Mock
  SynapseClientAsync mockSynapseClient;

  @Mock
  SynapseJavascriptClient mockJsClient;

  @Mock
  SynapseProperties mockSynapseProperties;

  StorageLocationWidget widget;

  @Mock
  SynapseAlert mockSynAlert;

  List<String> locationSettingBanners;

  @Mock
  EntityBundle mockBundle;

  Folder folder;

  @Mock
  CookieProvider mockCookies;

  @Mock
  EventBus mockEventBus;

  @Captor
  ArgumentCaptor<StorageLocationSetting> locationSettingCaptor;

  public static final String DEFAULT_STORAGE_ID = "424242";

  @Before
  public void setup() {
    when(
      mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
    )
      .thenReturn(null);
    widget =
      new StorageLocationWidget(
        mockView,
        mockSynapseClient,
        mockJsClient,
        mockSynAlert,
        mockSynapseProperties,
        mockCookies,
        mockEventBus
      );
    folder = new Folder();
    folder.setId("syn420");
    when(mockBundle.getEntity()).thenReturn(folder);
    locationSettingBanners =
      Arrays.asList(new String[] { "Banner 1", "Banner 2" });
    widget.configure(mockBundle);
    when(
      mockSynapseProperties.getSynapseProperty(
        WebConstants.DEFAULT_STORAGE_ID_PROPERTY_KEY
      )
    )
      .thenReturn(DEFAULT_STORAGE_ID);
  }

  @Test
  public void testConfigure() {
    verify(mockView).setSynAlertWidget(mockSynAlert);
    verify(mockView).setPresenter(widget);
    verify(mockSynAlert).clear();
    verify(mockView).clear();
    verify(mockView).setLoading(true);
  }

  @Test
  public void testGetMyLocationSettingBanners() {
    AsyncMockStubber
      .callSuccessWith(locationSettingBanners)
      .when(mockSynapseClient)
      .getMyLocationSettingBanners(any(AsyncCallback.class));
    widget.getMyLocationSettingBanners();
    verify(mockView).setBannerDropdownVisible(true);
    verify(mockView).setBannerSuggestions(anyList());
  }

  @Test
  public void testGetMyLocationSettingBannersEmpty() {
    locationSettingBanners = Collections.EMPTY_LIST;
    AsyncMockStubber
      .callSuccessWith(locationSettingBanners)
      .when(mockSynapseClient)
      .getMyLocationSettingBanners(any(AsyncCallback.class));
    widget.getMyLocationSettingBanners();
    verify(mockView).setBannerDropdownVisible(false);
    verify(mockView).setBannerSuggestions(anyList());
  }

  @Test
  public void testGetMyLocationSettingBannersFailure() {
    String error = "An service error that should be shown to the user";
    AsyncMockStubber
      .callFailureWith(new Exception(error))
      .when(mockSynapseClient)
      .getMyLocationSettingBanners(any(AsyncCallback.class));
    widget.getMyLocationSettingBanners();
    verify(mockView).showErrorMessage(error);
    verify(mockView).hide();
  }

  @Test
  public void testGetStorageLocationSettingNull() {
    UploadDestination entityStorageLocationSetting = null;
    AsyncMockStubber
      .callSuccessWith(entityStorageLocationSetting)
      .when(mockJsClient)
      .getDefaultUploadDestination(anyString(), any(AsyncCallback.class));
    reset(mockView);
    widget.getStorageLocationSetting();
    // should remain set to the default config
    verify(mockView).setLoading(false);
    verify(mockView).setS3StsVisible(false);
    verifyNoMoreInteractions(mockView);
  }

  @Test
  public void testGetStorageLocationSettingFailure() {
    String error = "An service error that should be shown to the user";
    AsyncMockStubber
      .callFailureWith(new Exception(error))
      .when(mockJsClient)
      .getDefaultUploadDestination(anyString(), any(AsyncCallback.class));
    widget.getStorageLocationSetting();
    verify(mockView).showErrorMessage(error);
    verify(mockView).setLoading(false);
  }

  @Test
  public void testNullBanner() {
    ExternalS3UploadDestination entityStorageLocationSetting = new ExternalS3UploadDestination();
    String baseKey = "key";
    String bucket = "a.bucket     ";
    String banner = null;
    entityStorageLocationSetting.setBanner(banner);
    entityStorageLocationSetting.setBucket(bucket);
    entityStorageLocationSetting.setBaseKey(baseKey);
    AsyncMockStubber
      .callSuccessWith(entityStorageLocationSetting)
      .when(mockJsClient)
      .getDefaultUploadDestination(anyString(), any(AsyncCallback.class));
    widget.getStorageLocationSetting();
    verify(mockView).setExternalS3Banner("");
  }

  @Test
  public void testGetStorageLocationSettingExternalS3() {
    when(
      mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
    )
      .thenReturn("true");
    ExternalS3UploadDestination entityStorageLocationSetting = new ExternalS3UploadDestination();
    String baseKey = "key";
    String bucket = "a.bucket     ";
    String banner = "upload to a.bucket";
    Boolean isStsEnabled = false;
    entityStorageLocationSetting.setBanner(banner);
    entityStorageLocationSetting.setBucket(bucket);
    entityStorageLocationSetting.setBaseKey(baseKey);
    entityStorageLocationSetting.setStsEnabled(isStsEnabled);
    AsyncMockStubber
      .callSuccessWith(entityStorageLocationSetting)
      .when(mockJsClient)
      .getDefaultUploadDestination(anyString(), any(AsyncCallback.class));
    widget.getStorageLocationSetting();
    verify(mockView).setS3BaseKey(baseKey);
    verify(mockView).setS3Bucket(bucket.trim());
    verify(mockView).setExternalS3Banner(banner);
    verify(mockView).selectExternalS3Storage();
    verify(mockView).setS3StsVisible(true); //because we're in alpha mode
    verify(mockView).setS3StsEnabled(isStsEnabled);
  }

  @Test
  public void testS3StsEnabled() {
    ExternalS3UploadDestination entityStorageLocationSetting = new ExternalS3UploadDestination();
    entityStorageLocationSetting.setBanner("");
    entityStorageLocationSetting.setBucket("");
    entityStorageLocationSetting.setBaseKey("");
    entityStorageLocationSetting.setStsEnabled(true);
    AsyncMockStubber
      .callSuccessWith(entityStorageLocationSetting)
      .when(mockJsClient)
      .getDefaultUploadDestination(anyString(), any(AsyncCallback.class));
    widget.getStorageLocationSetting();
    verify(mockView).selectExternalS3Storage();
    verify(mockView).setS3StsVisible(true);
    verify(mockView).setS3StsEnabled(true);
  }

  // This test can be deleted once STS is out of alpha mode
  @Test
  public void testS3StsNotEnabledNotInAlpha() {
    ExternalS3UploadDestination entityStorageLocationSetting = new ExternalS3UploadDestination();
    entityStorageLocationSetting.setBanner("");
    entityStorageLocationSetting.setBucket("");
    entityStorageLocationSetting.setBaseKey("");
    entityStorageLocationSetting.setStsEnabled(false);
    AsyncMockStubber
      .callSuccessWith(entityStorageLocationSetting)
      .when(mockJsClient)
      .getDefaultUploadDestination(anyString(), any(AsyncCallback.class));
    widget.getStorageLocationSetting();
    verify(mockView).selectExternalS3Storage();
    verify(mockView).setS3StsVisible(false);
    verify(mockView).setS3StsEnabled(false);
  }

  @Test
  public void testGetStorageLocationSettingHideSFTP() {
    ExternalS3UploadDestination entityStorageLocationSetting = new ExternalS3UploadDestination();
    entityStorageLocationSetting.setBanner("");
    entityStorageLocationSetting.setBucket("");
    entityStorageLocationSetting.setBaseKey("");
    AsyncMockStubber
      .callSuccessWith(entityStorageLocationSetting)
      .when(mockJsClient)
      .getDefaultUploadDestination(anyString(), any(AsyncCallback.class));
    widget.getStorageLocationSetting();
    verify(mockView).selectExternalS3Storage();
  }

  @Test
  public void testGetStorageLocationSettingGoogleCloud() {
    ExternalGoogleCloudUploadDestination entityStorageLocationSetting = new ExternalGoogleCloudUploadDestination();
    String bucket = "my-bucket";
    String baseKey = "key.txt";
    String banner = "upload to a google cloud bucket";
    entityStorageLocationSetting.setBucket(bucket);
    entityStorageLocationSetting.setBaseKey(baseKey);
    entityStorageLocationSetting.setBanner(banner);
    AsyncMockStubber
      .callSuccessWith(entityStorageLocationSetting)
      .when(mockJsClient)
      .getDefaultUploadDestination(anyString(), any(AsyncCallback.class));
    widget.getStorageLocationSetting();
    verify(mockView).setGoogleCloudBucket(bucket);
    verify(mockView).setGoogleCloudBaseKey(baseKey);
    verify(mockView).setExternalGoogleCloudBanner(banner);
    verify(mockView).selectExternalGoogleCloudStorage();
    verify(mockView, atLeast(1)).setGoogleCloudVisible(true);
  }

  @Test
  public void testGetStorageLocationSettingExternalObjectStore() {
    ExternalObjectStoreUploadDestination setting = new ExternalObjectStoreUploadDestination();
    String endpointUrl = "something.gov";
    String bucket = "mybucket";
    String banner = "upload to a sftp site";
    setting.setBanner(banner);
    setting.setBucket(bucket);
    setting.setEndpointUrl(endpointUrl);
    AsyncMockStubber
      .callSuccessWith(setting)
      .when(mockJsClient)
      .getDefaultUploadDestination(anyString(), any(AsyncCallback.class));
    widget.getStorageLocationSetting();
    verify(mockView).setExternalObjectStoreBanner(banner);
    verify(mockView).setExternalObjectStoreBucket(bucket);
    verify(mockView).setExternalObjectStoreEndpointUrl(endpointUrl);
    verify(mockView).selectExternalObjectStore();
  }

  @Test
  public void testShow() {
    widget.show();
    verify(mockView).show();
  }

  @Test
  public void testHide() {
    widget.hide();
    verify(mockView).hide();
  }

  @Test
  public void testOnSaveSynapseStorage() {
    when(mockView.isExternalS3StorageSelected()).thenReturn(false);
    AsyncMockStubber
      .callSuccessWith(null)
      .when(mockSynapseClient)
      .createStorageLocationSetting(
        anyString(),
        any(StorageLocationSetting.class),
        any(AsyncCallback.class)
      );
    widget.onSave();

    verify(mockSynapseClient)
      .createStorageLocationSetting(
        anyString(),
        any(StorageLocationSetting.class),
        any(AsyncCallback.class)
      );
    verify(mockEventBus).fireEvent(any(EntityUpdatedEvent.class));
    verify(mockView).hide();
  }

  @Test
  public void testOnSaveExternalObjectStore() {
    when(mockView.isExternalS3StorageSelected()).thenReturn(false);
    when(mockView.isSynapseStorageSelected()).thenReturn(false);
    when(mockView.isExternalObjectStoreSelected()).thenReturn(true);
    String banner = "hello object store";
    String bucket = "need a bucket";
    String endpointUrl = "http://test";
    when(mockView.getExternalObjectStoreBanner()).thenReturn(banner);
    when(mockView.getExternalObjectStoreBucket()).thenReturn(bucket);
    when(mockView.getExternalObjectStoreEndpointUrl()).thenReturn(endpointUrl);
    AsyncMockStubber
      .callSuccessWith(null)
      .when(mockSynapseClient)
      .createStorageLocationSetting(
        anyString(),
        any(StorageLocationSetting.class),
        any(AsyncCallback.class)
      );

    widget.onSave();

    verify(mockSynapseClient)
      .createStorageLocationSetting(
        anyString(),
        locationSettingCaptor.capture(),
        any(AsyncCallback.class)
      );
    ExternalObjectStorageLocationSetting setting = (ExternalObjectStorageLocationSetting) locationSettingCaptor.getValue();
    assertEquals(banner, setting.getBanner());
    assertEquals(endpointUrl, setting.getEndpointUrl());
    assertEquals(bucket, setting.getBucket());
    assertEquals(UploadType.S3, setting.getUploadType());
    verify(mockEventBus).fireEvent(any(EntityUpdatedEvent.class));
    verify(mockView).hide();
  }

  @Test
  public void testOnSaveSynapseStorageFailure() {
    when(mockView.isExternalS3StorageSelected()).thenReturn(false);
    Exception e = new Exception("oh nos!");
    AsyncMockStubber
      .callFailureWith(e)
      .when(mockSynapseClient)
      .createStorageLocationSetting(
        anyString(),
        any(StorageLocationSetting.class),
        any(AsyncCallback.class)
      );
    widget.onSave();

    verify(mockSynapseClient)
      .createStorageLocationSetting(
        anyString(),
        any(StorageLocationSetting.class),
        any(AsyncCallback.class)
      );
    verify(mockSynAlert).handleException(e);
  }

  @Test
  public void testOnSaveExternalS3() {
    when(mockView.isExternalS3StorageSelected()).thenReturn(true);
    String baseKey = "   key";
    String bucket = "a.bucket     ";
    String banner = "  upload to a.bucket";
    when(mockView.getExternalS3Banner()).thenReturn(banner);
    when(mockView.getS3Bucket()).thenReturn(bucket);
    when(mockView.getS3BaseKey()).thenReturn(baseKey);
    widget.onSave();
    ArgumentCaptor<StorageLocationSetting> captor = ArgumentCaptor.forClass(
      StorageLocationSetting.class
    );
    verify(mockSynapseClient)
      .createStorageLocationSetting(
        anyString(),
        captor.capture(),
        any(AsyncCallback.class)
      );

    ExternalS3StorageLocationSetting capturedSetting = (ExternalS3StorageLocationSetting) captor.getValue();
    assertEquals(baseKey.trim(), capturedSetting.getBaseKey());
    assertEquals(bucket.trim(), capturedSetting.getBucket());
    assertEquals(banner.trim(), capturedSetting.getBanner());
  }

  @Test
  public void testOnSaveExternalS3Invalid() {
    when(mockView.isExternalS3StorageSelected()).thenReturn(true);
    when(mockView.getExternalS3Banner()).thenReturn("banner");
    // invalid bucket
    when(mockView.getS3Bucket()).thenReturn("   ");
    when(mockView.getS3BaseKey()).thenReturn("base key");
    widget.onSave();
    verify(mockSynAlert).showError(anyString());
  }

  @Test
  public void testOnSaveExternalGoogleCloud() {
    when(mockView.isExternalGoogleCloudStorageSelected()).thenReturn(true);
    String baseKey = "   key";
    String bucket = "a.bucket     ";
    String banner = "  upload to a.bucket";
    when(mockView.getExternalGoogleCloudBanner()).thenReturn(banner);
    when(mockView.getGoogleCloudBucket()).thenReturn(bucket);
    when(mockView.getGoogleCloudBaseKey()).thenReturn(baseKey);
    widget.onSave();
    ArgumentCaptor<StorageLocationSetting> captor = ArgumentCaptor.forClass(
      StorageLocationSetting.class
    );
    verify(mockSynapseClient)
      .createStorageLocationSetting(
        anyString(),
        captor.capture(),
        any(AsyncCallback.class)
      );

    ExternalGoogleCloudStorageLocationSetting capturedSetting = (ExternalGoogleCloudStorageLocationSetting) captor.getValue();
    assertEquals(baseKey.trim(), capturedSetting.getBaseKey());
    assertEquals(bucket.trim(), capturedSetting.getBucket());
    assertEquals(banner.trim(), capturedSetting.getBanner());
  }

  @Test
  public void testOnSaveExternalGoogleCloudInvalid() {
    when(mockView.isExternalGoogleCloudStorageSelected()).thenReturn(true);
    when(mockView.getExternalGoogleCloudBanner()).thenReturn("banner");
    // invalid bucket
    when(mockView.getGoogleCloudBucket()).thenReturn("   ");
    when(mockView.getGoogleCloudBaseKey()).thenReturn("base key");
    widget.onSave();
    verify(mockSynAlert).showError(anyString());
  }
}
