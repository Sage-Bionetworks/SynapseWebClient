package org.sagebionetworks.web.unitclient.widget.entity.file;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.file.ExternalFileHandle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.widget.clienthelp.FileClientsHelp;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.file.FileDownloadButton;
import org.sagebionetworks.web.client.widget.entity.file.FileDownloadButtonView;
import org.sagebionetworks.web.client.widget.licenseddownloader.LicensedDownloader;
import org.sagebionetworks.web.client.widget.login.LoginModalWidget;
import org.sagebionetworks.web.shared.WebConstants;
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
	LicensedDownloader mockLicensedDownloader; 
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
	FileDownloadButton widget;
	List<FileHandle> fileHandles;
	
	public static final String SFTP_ENDPOINT = "https://sftp.org/sftp";
	public static final String SFTP_HOST = "my.sftp.server";
	public static final String ENTITY_ID = "syn210";
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new FileDownloadButton(mockView, mockSynapseClient, mockLicensedDownloader, mockLoginModalWidget, mockGlobalAppState, mockSynAlert, mockGinInjector);
		when(mockGlobalAppState.getSynapseProperty(WebConstants.SFTP_PROXY_ENDPOINT)).thenReturn(SFTP_ENDPOINT);
		when(mockEntityBundle.getEntity()).thenReturn(mockFileEntity);
		when(mockFileEntity.getId()).thenReturn(ENTITY_ID);
		fileHandles = new ArrayList<FileHandle>();
		when(mockEntityBundle.getFileHandles()).thenReturn(fileHandles);
		when(mockGinInjector.getFileClientsHelp()).thenReturn(mockFileClientsHelp);
		AsyncMockStubber.callSuccessWith(SFTP_HOST).when(mockSynapseClient).getHost(anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockView).setSynAlert(any(IsWidget.class));
		verify(mockLicensedDownloader).setEntityUpdatedHandler(any(EntityUpdatedHandler.class));
		verify(mockLoginModalWidget).setPrimaryButtonText(DisplayConstants.BUTTON_DOWNLOAD);
	}

	@Test
	public void testConfigureDirectDownloadLink() {
		String directDownloadURL = "http://filehandle.servlet/filehandleid=1";
		when(mockLicensedDownloader.getDirectDownloadURL()).thenReturn(directDownloadURL);
		widget.configure(mockEntityBundle);
		verify(mockView).clear();
		verify(mockView).setDirectDownloadLink(directDownloadURL);
		verify(mockView).setDirectDownloadLinkVisible(true);
		verify(mockView).setClientsHelpVisible(false);
		verify(mockView).setClientsHelpVisible(true);
		
		verify(mockFileClientsHelp).configure(ENTITY_ID);
	}
	
	@Test
	public void testConfigureSftpLink() {
		String dataFileHandleId = "3333";
		when(mockFileEntity.getDataFileHandleId()).thenReturn(dataFileHandleId);
		when(mockFileHandle.getId()).thenReturn(dataFileHandleId);
		fileHandles.add(mockFileHandle);
		
		String fileUrl = SFTP_ENDPOINT + "/path=mysftpfile.txt";
		when(mockLicensedDownloader.getDirectDownloadURL()).thenReturn(fileUrl);
		when(mockFileHandle.getExternalURL()).thenReturn(fileUrl);
		widget.configure(mockEntityBundle);
		verify(mockView).setClientsHelpVisible(false);
		verify(mockView).setAuthorizedDirectDownloadLinkVisible(true);
		verify(mockFileClientsHelp).configure(ENTITY_ID);
		verify(mockLoginModalWidget).configure(fileUrl,  FormPanel.METHOD_POST, FormPanel.ENCODING_MULTIPART);
		verify(mockSynapseClient).getHost(anyString(), any(AsyncCallback.class));
		verify(mockLoginModalWidget).setInstructionMessage(DisplayConstants.DOWNLOAD_CREDENTIALS_REQUIRED + SFTP_HOST);
	}
	
	@Test
	public void testLicensedDownloadLink() {
		when(mockLicensedDownloader.getDirectDownloadURL()).thenReturn(null);
		widget.configure(mockEntityBundle);
		verify(mockView).setClientsHelpVisible(false);
		verify(mockView).setLicensedDownloadLinkVisible(true);
		verify(mockFileClientsHelp).configure(ENTITY_ID);
	}

	@Test
	public void testFireEntityUpdatedEvent() {
		widget.setEntityUpdatedHandler(mockEntityUpdatedHandler);
		widget.fireEntityUpdatedEvent(mockEntityUpdatedEvent);
		verify(mockEntityUpdatedHandler).onPersistSuccess(mockEntityUpdatedEvent);
	}

	@Test
	public void testOnLicensedDownloadClick() {
		widget.onLicensedDownloadClick();
		verify(mockLicensedDownloader).onDownloadButtonClicked();
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
