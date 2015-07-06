package org.sagebionetworks.web.unitclient.widget.entity.controller;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.project.ExternalS3StorageLocationSetting;
import org.sagebionetworks.repo.model.project.ExternalStorageLocationSetting;
import org.sagebionetworks.repo.model.project.StorageLocationSetting;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.controller.EntityRefProvEntryView;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceListWidget;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceListWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceURLDialogWidget;
import org.sagebionetworks.web.client.widget.entity.controller.StorageLocationWidget;
import org.sagebionetworks.web.client.widget.entity.controller.StorageLocationWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.controller.URLProvEntryView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class StorageLocationWidgetTest {

	StorageLocationWidgetView mockView;
	SynapseClientAsync mockSynapseClient;
	StorageLocationWidget widget;
	SynapseAlert mockSynAlert;
	List<String> locationSettingBanners;
	EntityBundle mockBundle;
	Folder folder;
	EntityUpdatedHandler mockEntityUpdatedHandler;
	
	@Before
	public void setup() {
		mockView = mock(StorageLocationWidgetView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockSynAlert = mock(SynapseAlert.class);
		widget = new StorageLocationWidget(mockView, mockSynapseClient, mockSynAlert);
		mockBundle = mock(EntityBundle.class);
		folder = new Folder();
		folder.setId("syn420");
		when(mockBundle.getEntity()).thenReturn(folder);
		locationSettingBanners = Arrays.asList(new String[]{"Banner 1", "Banner 2"});
		mockEntityUpdatedHandler = mock(EntityUpdatedHandler.class);
		widget.configure(mockBundle, mockEntityUpdatedHandler);
	}
	
	@Test
	public void testConfigure() {
		verify(mockView).setSynAlertWidget(mockSynAlert);
		verify(mockView).setPresenter(widget);
		verify(mockSynAlert).clear();
		verify(mockView).clear();
	}

	@Test
	public void testGetMyLocationSettingBanners() {
		AsyncMockStubber.callSuccessWith(locationSettingBanners).when(mockSynapseClient).getMyLocationSettingBanners(any(AsyncCallback.class));
		widget.getMyLocationSettingBanners();
		verify(mockView).setBannerDropdownVisible(true);
		verify(mockView).setBannerSuggestions(anyList());
	}
	
	@Test
	public void testGetMyLocationSettingBannersEmpty() {
		locationSettingBanners = Collections.EMPTY_LIST;
		AsyncMockStubber.callSuccessWith(locationSettingBanners).when(mockSynapseClient).getMyLocationSettingBanners(any(AsyncCallback.class));
		widget.getMyLocationSettingBanners();
		verify(mockView).setBannerDropdownVisible(false);
		verify(mockView).setBannerSuggestions(anyList());
	}
	
	@Test
	public void testGetMyLocationSettingBannersFailure() {
		String error= "An service error that should be shown to the user";
		AsyncMockStubber.callFailureWith(new Exception(error)).when(mockSynapseClient).getMyLocationSettingBanners(any(AsyncCallback.class));
		widget.getMyLocationSettingBanners();
		verify(mockView).showErrorMessage(error);
		verify(mockView).hide();
	}
	
	@Test
	public void testGetStorageLocationSettingNull() {
		StorageLocationSetting entityStorageLocationSetting = null;
		AsyncMockStubber.callSuccessWith(entityStorageLocationSetting).when(mockSynapseClient).getStorageLocationSetting(anyString(), any(AsyncCallback.class));
		reset(mockView);
		widget.getStorageLocationSetting();
		//should remain set to the default config
		verifyZeroInteractions(mockView);
	}
	
	@Test
	public void testGetStorageLocationSettingFailure() {
		String error= "An service error that should be shown to the user";
		AsyncMockStubber.callFailureWith(new Exception(error)).when(mockSynapseClient).getStorageLocationSetting(anyString(), any(AsyncCallback.class));
		widget.getStorageLocationSetting();
		verify(mockView).showErrorMessage(error);
		verify(mockView).hide();
	}
	
	@Test
	public void testGetStorageLocationSettingExternalS3() {
		ExternalS3StorageLocationSetting entityStorageLocationSetting = new ExternalS3StorageLocationSetting();
		String baseKey = "key";
		String bucket = "a.bucket     ";
		String banner = "upload to a.bucket";
		entityStorageLocationSetting.setBanner(banner);
		entityStorageLocationSetting.setBucket(bucket);
		entityStorageLocationSetting.setBaseKey(baseKey);
		AsyncMockStubber.callSuccessWith(entityStorageLocationSetting).when(mockSynapseClient).getStorageLocationSetting(anyString(), any(AsyncCallback.class));
		widget.getStorageLocationSetting();
		verify(mockView).setBaseKey(baseKey);
		verify(mockView).setBucket(bucket.trim());
		verify(mockView).setExternalS3Banner(banner);
		verify(mockView).selectExternalS3Storage();
	}
	
	@Test
	public void testGetStorageLocationSettingSFTP() {
		ExternalStorageLocationSetting entityStorageLocationSetting = new ExternalStorageLocationSetting();
		String url = "sftp://tcgaftps.nnn.mmm.gov";
		String banner = "upload to a sftp site";
		entityStorageLocationSetting.setBanner(banner);
		entityStorageLocationSetting.setUrl(url);
		AsyncMockStubber.callSuccessWith(entityStorageLocationSetting).when(mockSynapseClient).getStorageLocationSetting(anyString(), any(AsyncCallback.class));
		widget.getStorageLocationSetting();
		verify(mockView).setSFTPBanner(banner);
		verify(mockView).setSFTPUrl(url);
		verify(mockView).selectSFTPStorage();
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
		when(mockView.isSFTPStorageSelected()).thenReturn(false);
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).createStorageLocationSetting(anyString(), any(StorageLocationSetting.class), any(AsyncCallback.class));
		widget.onSave();
		
		verify(mockSynapseClient).createStorageLocationSetting(anyString(), any(StorageLocationSetting.class), any(AsyncCallback.class));
		verify(mockEntityUpdatedHandler).onPersistSuccess(any(EntityUpdatedEvent.class));
		verify(mockView).hide();
	}
	
	@Test
	public void testOnSaveSynapseStorageFailure() {
		when(mockView.isExternalS3StorageSelected()).thenReturn(false);
		when(mockView.isSFTPStorageSelected()).thenReturn(false);
		Exception e = new Exception("oh nos!");
		AsyncMockStubber.callFailureWith(e).when(mockSynapseClient).createStorageLocationSetting(anyString(), any(StorageLocationSetting.class), any(AsyncCallback.class));
		widget.onSave();
		
		verify(mockSynapseClient).createStorageLocationSetting(anyString(), any(StorageLocationSetting.class), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(e);
	}

	@Test
	public void testOnSaveExternalS3() {
		when(mockView.isExternalS3StorageSelected()).thenReturn(true);
		when(mockView.isSFTPStorageSelected()).thenReturn(false);
		String baseKey = "   key";
		String bucket = "a.bucket     ";
		String banner = "  upload to a.bucket";
		when(mockView.getExternalS3Banner()).thenReturn(banner);
		when(mockView.getBucket()).thenReturn(bucket);
		when(mockView.getBaseKey()).thenReturn(baseKey);
		widget.onSave();
		ArgumentCaptor<StorageLocationSetting> captor = ArgumentCaptor.forClass(StorageLocationSetting.class);
		verify(mockSynapseClient).createStorageLocationSetting(anyString(), captor.capture(), any(AsyncCallback.class));
		
		ExternalS3StorageLocationSetting capturedSetting = (ExternalS3StorageLocationSetting) captor.getValue();
		assertEquals(baseKey.trim(), capturedSetting.getBaseKey());
		assertEquals(bucket.trim(), capturedSetting.getBucket());
		assertEquals(banner.trim(), capturedSetting.getBanner());
	}
	
	@Test
	public void testOnSaveExternalS3Invalid() {
		when(mockView.isExternalS3StorageSelected()).thenReturn(true);
		when(mockView.isSFTPStorageSelected()).thenReturn(false);
		when(mockView.getExternalS3Banner()).thenReturn("banner");
		//invalid bucket
		when(mockView.getBucket()).thenReturn("   ");
		when(mockView.getBaseKey()).thenReturn("base key");
		widget.onSave();
		verify(mockSynAlert).showError(anyString());
	}


	@Test
	public void testOnSaveSFTP() {
		when(mockView.isExternalS3StorageSelected()).thenReturn(false);
		when(mockView.isSFTPStorageSelected()).thenReturn(true);
		String url = "sftp://tcgaftps.nnn.mmm.gov";
		String banner = "a sftp site";
		when(mockView.getSFTPUrl()).thenReturn(url);
		when(mockView.getSFTPBanner()).thenReturn(banner);
		widget.onSave();
		
		ArgumentCaptor<StorageLocationSetting> captor = ArgumentCaptor.forClass(StorageLocationSetting.class);
		verify(mockSynapseClient).createStorageLocationSetting(anyString(), captor.capture(), any(AsyncCallback.class));
		
		ExternalStorageLocationSetting capturedSetting = (ExternalStorageLocationSetting) captor.getValue();
		assertEquals(url.trim(), capturedSetting.getUrl());
		assertEquals(banner.trim(), capturedSetting.getBanner());
	}
	
	@Test
	public void testOnSaveSFTPInvalid1() {
		when(mockView.isExternalS3StorageSelected()).thenReturn(false);
		when(mockView.isSFTPStorageSelected()).thenReturn(true);
		
		when(mockView.getSFTPUrl()).thenReturn("https://fjkdsljfdsl");
		when(mockView.getSFTPBanner()).thenReturn("banner");
		widget.onSave();
		verify(mockSynAlert).showError(anyString());
	}

	@Test
	public void testOnSaveSFTPInvalid2() {
		when(mockView.isExternalS3StorageSelected()).thenReturn(false);
		when(mockView.isSFTPStorageSelected()).thenReturn(true);
		//empty
		when(mockView.getSFTPUrl()).thenReturn("   ");
		when(mockView.getSFTPBanner()).thenReturn("banner");
		widget.onSave();
		verify(mockSynAlert).showError(anyString());
	}

}
