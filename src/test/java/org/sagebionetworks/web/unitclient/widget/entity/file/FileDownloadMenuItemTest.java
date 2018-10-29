package org.sagebionetworks.web.unitclient.widget.entity.file;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.RestrictionInformationResponse;
import org.sagebionetworks.repo.model.file.ExternalFileHandle;
import org.sagebionetworks.repo.model.file.ExternalObjectStoreFileHandle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.place.AccessRequirementsPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.aws.AwsSdk;
import org.sagebionetworks.web.client.widget.entity.file.FileDownloadMenuItem;
import org.sagebionetworks.web.client.widget.entity.file.FileDownloadMenuItemView;
import org.sagebionetworks.web.client.widget.login.LoginModalWidget;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FormPanel;

public class FileDownloadMenuItemTest {
	@Mock
	FileDownloadMenuItemView mockView;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	LoginModalWidget mockLoginModalWidget;
	@Mock
	SynapseProperties mockSynapseProperties;
	@Mock
	PortalGinInjector mockGinInjector;
	
	@Mock
	EntityBundle mockEntityBundle;
	@Mock
	FileEntity mockFileEntity;
	@Mock
	ExternalFileHandle mockFileHandle;
	@Mock
	ExternalObjectStoreFileHandle mockObjectStoreFileHandle;
	@Mock
	EntityUpdatedEvent mockEntityUpdatedEvent;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	SynapseJSNIUtils mockJsniUtils;
	@Mock
	GWTWrapper mockGwt;
	@Mock
	CookieProvider mockCookies;
	@Mock
	RestrictionInformationResponse mockRestrictionInformation;
	@Mock
	AwsSdk mockAwsSdk;
	@Mock
	PopupUtilsView mockPopupUtilsView;
	@Mock
	JavaScriptObject mockS3;
	@Captor
	ArgumentCaptor<CallbackP> callbackPCaptor;
	
	FileDownloadMenuItem widget;
	List<FileHandle> fileHandles;
	
	public static final String SFTP_ENDPOINT = "https://sftp.org/sftp";
	public static final String SFTP_HOST = "my.sftp.server";
	public static final String ENTITY_ID = "syn210";
	public static final Long VERSION = 32L;
	public static final String fileHandleAssociationUrl="http://mytestfilehandleassociationurl/filehandleassociation";
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new FileDownloadMenuItem(mockView, mockSynapseClient, mockLoginModalWidget, mockGinInjector,
				mockSynapseJavascriptClient, mockAuthController, mockJsniUtils, mockGwt, mockCookies, mockAwsSdk, mockPopupUtilsView);
		when(mockSynapseProperties.getSynapseProperty(WebConstants.SFTP_PROXY_ENDPOINT)).thenReturn(SFTP_ENDPOINT);
		when(mockEntityBundle.getEntity()).thenReturn(mockFileEntity);
		when(mockFileEntity.getId()).thenReturn(ENTITY_ID);
		when(mockFileEntity.getVersionNumber()).thenReturn(VERSION);
		fileHandles = new ArrayList<FileHandle>();
		when(mockEntityBundle.getFileHandles()).thenReturn(fileHandles);
		when(mockGinInjector.getSynapseProperties()).thenReturn(mockSynapseProperties);
		AsyncMockStubber.callSuccessWith(SFTP_HOST).when(mockSynapseClient).getHost(anyString(), any(AsyncCallback.class));
		when(mockJsniUtils.getFileHandleAssociationUrl(anyString(), any(FileHandleAssociateType.class), anyString())).thenReturn(fileHandleAssociationUrl);
		when(mockRestrictionInformation.getHasUnmetAccessRequirement()).thenReturn(false);
	}
	
	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockLoginModalWidget).setPrimaryButtonText(DisplayConstants.BUTTON_DOWNLOAD);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadFileDownloadUrl() throws RestServiceException {
		// Null locations
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		when(mockEntityBundle.getFileHandles()).thenReturn(null);
		widget.configure(mockEntityBundle, mockRestrictionInformation);
		assertNull(widget.getFileHandle());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testLoadFileDownloadUrlAnonymous() throws RestServiceException {
		// Not Logged in Test: Download
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		widget.configure(mockEntityBundle, mockRestrictionInformation);
		verify(mockView).setIsDirectDownloadLink(FileDownloadMenuItem.LOGIN_PLACE_LINK);
		assertNull(widget.getFileHandle());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testLoadFileDownloadUrlSuccess() throws RestServiceException {
		// Success Test: Download
		String fileHandleId = "22";
		S3FileHandle fileHandle = new S3FileHandle();
		fileHandle.setContentMd5("myContentMd5");
		fileHandle.setFileName("myFileName.png");
		fileHandle.setId(fileHandleId);
		List fileHandles = new ArrayList<FileHandle>();
		fileHandles.add(fileHandle);
		when(mockEntityBundle.getFileHandles()).thenReturn(fileHandles);
		when(mockFileEntity.getDataFileHandleId()).thenReturn(fileHandleId);
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		widget.configure(mockEntityBundle, mockRestrictionInformation);
		assertNotNull(widget.getFileHandle());
		verify(mockView).setIsDirectDownloadLink(fileHandleAssociationUrl);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testLoadFileDownloadUrlExternal() throws RestServiceException {
		// Success Test: External file
		String fileHandleId = "22";
		ExternalFileHandle externalFileHandle = new ExternalFileHandle();
		externalFileHandle.setFileName("myExternalFileName.png");
		externalFileHandle.setId(fileHandleId);
		String url = "http://getbootstrap.com/javascript/";
		externalFileHandle.setExternalURL(url);
		
		fileHandles = new ArrayList<FileHandle>();
		fileHandles.add(externalFileHandle);
		when(mockEntityBundle.getFileHandles()).thenReturn(fileHandles);
		when(mockFileEntity.getDataFileHandleId()).thenReturn(fileHandleId);
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		widget.configure(mockEntityBundle, mockRestrictionInformation);
		assertNotNull(widget.getFileHandle());
		verify(mockView).setIsDirectDownloadLink(url);
	}
	
	@Test
	public void testConfigureSftpLink() {
		String dataFileHandleId = "3333";
		when(mockFileEntity.getDataFileHandleId()).thenReturn(dataFileHandleId);
		when(mockFileHandle.getId()).thenReturn(dataFileHandleId);
		fileHandles.add(mockFileHandle);
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		String fileUrl = SFTP_ENDPOINT + "/path=mysftpfile.txt";
		when(mockFileHandle.getExternalURL()).thenReturn(fileUrl);
		widget.configure(mockEntityBundle, mockRestrictionInformation);
		verify(mockView).setIsAuthorizedDirectDownloadLink();
		verify(mockLoginModalWidget).configure(fileUrl,  FormPanel.METHOD_POST, FormPanel.ENCODING_MULTIPART);
		verify(mockSynapseClient).getHost(anyString(), any(AsyncCallback.class));
		verify(mockLoginModalWidget).setInstructionMessage(DisplayConstants.DOWNLOAD_CREDENTIALS_REQUIRED + SFTP_HOST);
	}
	
	@Test
	public void testConfigureExternalObjectStoreFileHandle() {
		String dataFileHandleId = "3333";
		String endpointUrl = "https://test.test.test";
		String bucket = "bucket";
		String fileKey = "9876/test.txt";
		String fileName = "file.txt";
		
		when(mockFileEntity.getDataFileHandleId()).thenReturn(dataFileHandleId);
		when(mockObjectStoreFileHandle.getId()).thenReturn(dataFileHandleId);
		when(mockObjectStoreFileHandle.getEndpointUrl()).thenReturn(endpointUrl);
		when(mockObjectStoreFileHandle.getFileKey()).thenReturn(fileKey);
		when(mockObjectStoreFileHandle.getBucket()).thenReturn(bucket);
		when(mockObjectStoreFileHandle.getFileName()).thenReturn(fileName);
		fileHandles.add(mockObjectStoreFileHandle);
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		widget.configure(mockEntityBundle, mockRestrictionInformation);
		verify(mockView).setIsUnauthenticatedS3DirectDownload();
		
		//under this configuration, try clicking the download button (verify login dialog shown)
		widget.onUnauthenticatedS3DirectDownloadClicked();
		verify(mockView).showLoginS3DirectDownloadDialog(endpointUrl);
		
		//after login
		String accessKeyId = "87652";
		String secretAccessKey = "12345";
		widget.onLoginS3DirectDownloadClicked(accessKeyId, secretAccessKey);
		
		verify(mockAwsSdk).getS3(eq(accessKeyId), eq(secretAccessKey), eq(bucket), eq(endpointUrl), callbackPCaptor.capture());
		callbackPCaptor.getValue().invoke(mockS3);
		//after s3 connection is established, show the final DOWNLOAD button
		verify(mockView).showS3DirectDownloadDialog();
		
		//simulate user clicking the last button
		String presignedUrl = "https://yourstorage/test.txt?signature=a&expiration=b";
		when(mockAwsSdk.getPresignedURL(anyString(), anyString(), anyString(), any(JavaScriptObject.class))).thenReturn(presignedUrl);
		widget.onAuthenticatedS3DirectDownloadClicked();
		verify(mockAwsSdk).getPresignedURL(fileKey, bucket, fileName, mockS3);
		verify(mockPopupUtilsView).openInNewWindow(presignedUrl);
	}
	
	@Test
	public void testLicensedDownloadLink() {
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		when(mockRestrictionInformation.getHasUnmetAccessRequirement()).thenReturn(true);
		widget.configure(mockEntityBundle, mockRestrictionInformation);
		
		verify(mockView).setIsDirectDownloadLink(FileDownloadMenuItem.ACCESS_REQUIREMENTS_LINK + ENTITY_ID + "&" + AccessRequirementsPlace.TYPE_PARAM + "=" + RestrictableObjectType.ENTITY.toString());
		assertNull(widget.getFileHandle());
	}

	@Test
	public void testOnAuthorizedDirectDownloadClicked() {
		widget.onAuthorizedDirectDownloadClicked();
		verify(mockLoginModalWidget).showModal();
	}
}
