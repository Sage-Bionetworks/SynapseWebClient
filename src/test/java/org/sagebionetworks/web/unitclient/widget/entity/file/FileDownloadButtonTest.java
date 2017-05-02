package org.sagebionetworks.web.unitclient.widget.entity.file;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.RestrictionInformation;
import org.sagebionetworks.repo.model.file.ExternalFileHandle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.clienthelp.FileClientsHelp;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.file.FileDownloadButton;
import org.sagebionetworks.web.client.widget.entity.file.FileDownloadButtonView;
import org.sagebionetworks.web.client.widget.login.LoginModalWidget;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.IsWidget;

public class FileDownloadButtonTest {
	@Mock
	FileDownloadButtonView mockView;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	LoginModalWidget mockLoginModalWidget;
	@Mock
	GlobalApplicationState mockGlobalAppState;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	PortalGinInjector mockGinInjector;
	
	@Mock
	EntityBundle mockEntityBundle;
	@Mock
	FileEntity mockFileEntity;
	@Mock
	ExternalFileHandle mockFileHandle;
	@Mock
	FileClientsHelp mockFileClientsHelp;
	@Mock
	EntityUpdatedHandler mockEntityUpdatedHandler;
	@Mock
	EntityUpdatedEvent mockEntityUpdatedEvent;
	@Mock
	DataAccessClientAsync mockDataAccessClient;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	SynapseJSNIUtils mockJsniUtils;
	@Mock
	GWTWrapper mockGwt;
	@Mock
	CookieProvider mockCookies;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	RestrictionInformation mockRestrictionInformation;
	FileDownloadButton widget;
	List<FileHandle> fileHandles;
	
	public static final String SFTP_ENDPOINT = "https://sftp.org/sftp";
	public static final String SFTP_HOST = "my.sftp.server";
	public static final String ENTITY_ID = "syn210";
	public static final String baseFileHandleUrl="http://mytestbasefilehandleurl/filehandle";
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new FileDownloadButton(mockView, mockSynapseClient, mockLoginModalWidget, mockGlobalAppState, mockSynAlert, mockGinInjector,
				mockDataAccessClient, mockAuthController, mockJsniUtils, mockGwt, mockCookies);
		when(mockGlobalAppState.getSynapseProperty(WebConstants.SFTP_PROXY_ENDPOINT)).thenReturn(SFTP_ENDPOINT);
		when(mockEntityBundle.getEntity()).thenReturn(mockFileEntity);
		when(mockFileEntity.getId()).thenReturn(ENTITY_ID);
		fileHandles = new ArrayList<FileHandle>();
		when(mockEntityBundle.getFileHandles()).thenReturn(fileHandles);
		when(mockGinInjector.getFileClientsHelp()).thenReturn(mockFileClientsHelp);
		AsyncMockStubber.callSuccessWith(SFTP_HOST).when(mockSynapseClient).getHost(anyString(), any(AsyncCallback.class));
		when(mockJsniUtils.getBaseFileHandleUrl()).thenReturn(baseFileHandleUrl);
		when(mockGlobalAppState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockRestrictionInformation.getHasUnmetAccessRequirement()).thenReturn(false);
	}
	
	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockView).setSynAlert(any(IsWidget.class));
		verify(mockLoginModalWidget).setPrimaryButtonText(DisplayConstants.BUTTON_DOWNLOAD);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadFileDownloadUrl() throws RestServiceException {
		// Null locations
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		when(mockEntityBundle.getFileHandles()).thenReturn(null);
		widget.configure(mockEntityBundle, mockRestrictionInformation);
		assertNull(widget.getDirectDownloadUrl());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testLoadFileDownloadUrlAnonymous() throws RestServiceException {
		// Not Logged in Test: Download
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		widget.configure(mockEntityBundle, mockRestrictionInformation);
		verify(mockView).setDirectDownloadLink(FileDownloadButton.LOGIN_PLACE_LINK);
		verify(mockView).setDirectDownloadLinkVisible(true);
		assertNull(widget.getDirectDownloadUrl());
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
		assertNotNull(widget.getDirectDownloadUrl());
		verify(mockView).setDirectDownloadLink("http://mytestbasefilehandleurl/filehandle?entityId=syn210&preview=false&proxy=false&xsrfToken=null&version=0");
		verify(mockView).setDirectDownloadLinkVisible(true);
		
		// First use in SWC of Mockito.InOrder. Supports multiple mocks. This verification correctly fails if you swap the lines below.
		InOrder inOrder = inOrder(mockView);
		inOrder.verify(mockView).setClientsHelpVisible(false);
		inOrder.verify(mockView).setClientsHelpVisible(true);
		
		verify(mockFileClientsHelp).configure(ENTITY_ID);
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
		assertNotNull(widget.getDirectDownloadUrl());
		verify(mockView).setDirectDownloadLink(url);
		verify(mockView).setDirectDownloadLinkVisible(true);
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
		verify(mockView).setClientsHelpVisible(false);
		verify(mockView).setAuthorizedDirectDownloadLinkVisible(true);
		verify(mockFileClientsHelp).configure(ENTITY_ID);
		verify(mockLoginModalWidget).configure(fileUrl,  FormPanel.METHOD_POST, FormPanel.ENCODING_MULTIPART);
		verify(mockSynapseClient).getHost(anyString(), any(AsyncCallback.class));
		verify(mockLoginModalWidget).setInstructionMessage(DisplayConstants.DOWNLOAD_CREDENTIALS_REQUIRED + SFTP_HOST);
	}
	
	@Test
	public void testLicensedDownloadLinkAlpha() {
		when(mockCookies.getCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY)).thenReturn("true");
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		when(mockRestrictionInformation.getHasUnmetAccessRequirement()).thenReturn(true);
		widget.configure(mockEntityBundle, mockRestrictionInformation);
		
		verify(mockView).setDirectDownloadLink(FileDownloadButton.ACCESS_REQUIREMENTS_LINK + ENTITY_ID);
		verify(mockView).setDirectDownloadLinkVisible(true);
		assertNull(widget.getDirectDownloadUrl());
	}

	@Test
	public void testFireEntityUpdatedEvent() {
		widget.setEntityUpdatedHandler(mockEntityUpdatedHandler);
		widget.fireEntityUpdatedEvent(mockEntityUpdatedEvent);
		verify(mockEntityUpdatedHandler).onPersistSuccess(mockEntityUpdatedEvent);
	}

	@Test
	public void testOnAuthorizedDirectDownloadClicked() {
		widget.onAuthorizedDirectDownloadClicked();
		verify(mockLoginModalWidget).showModal();
	}

	@Test
	public void testSetSize() {
		widget.setSize(ButtonSize.LARGE);
		verify(mockView).setButtonSize(ButtonSize.LARGE);
	}

}
