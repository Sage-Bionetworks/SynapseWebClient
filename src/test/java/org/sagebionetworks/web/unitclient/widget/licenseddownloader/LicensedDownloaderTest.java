package org.sagebionetworks.web.unitclient.widget.licenseddownloader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.repo.model.Study;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.file.ExternalFileHandle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.EntitySchemaCacheImpl;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.JiraClientAsync;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.AccessRequirementDialog;
import org.sagebionetworks.web.client.widget.entity.JiraGovernanceConstants;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelperImpl;
import org.sagebionetworks.web.client.widget.licenseddownloader.LicensedDownloader;
import org.sagebionetworks.web.client.widget.licenseddownloader.LicensedDownloaderView;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.unitclient.RegisterConstantsStub;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class LicensedDownloaderTest {
		
	LicensedDownloader licensedDownloader;
	LicensedDownloaderView mockView;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	SynapseClientAsync mockSynapseClient;
	GWTWrapper mockGwt;
	PlaceChanger mockPlaceChanger;
	AsyncCallback<String> mockStringCallback;

	JSONObjectAdapter jsonObjectAdapterProvider;
	EntityTypeProvider entityTypeProvider;
	FileEntity entity;
	EntityBundle entityBundle;
	Entity parentEntity;
	List<LocationData> locations;	
	EntityPath entityPath;
	JiraURLHelper jiraURLHelper;
	SynapseJSNIUtils mockSynapseJSNIUtils;
	AccessRequirementDialog mockAccessRequirementDialog;
	
	String baseFileHandleUrl="http://mytestbasefilehandleurl/filehandle";
	@SuppressWarnings("unchecked")
	@Before
	public void setup() throws UnsupportedEncodingException, JSONObjectAdapterException{		
		mockView = Mockito.mock(LicensedDownloaderView.class);		
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockGwt = mock(GWTWrapper.class);
		jsonObjectAdapterProvider = new JSONObjectAdapterImpl();
		mockStringCallback = mock(AsyncCallback.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockSynapseJSNIUtils = mock(SynapseJSNIUtils.class);

		// create entity type provider
		entityTypeProvider = new EntityTypeProvider(new RegisterConstantsStub(), new AdapterFactoryImpl(), new EntitySchemaCacheImpl(new AdapterFactoryImpl()));		

		JiraGovernanceConstants gc = mock(JiraGovernanceConstants.class);
		JiraClientAsync mockJiraClient = mock(JiraClientAsync.class);
		GWTWrapper mockGWTWrapper = mock(GWTWrapper.class);
		AuthenticationController mockAuthController = mock(AuthenticationController.class);
		jiraURLHelper  = new JiraURLHelperImpl(gc, mockJiraClient, mockGWTWrapper, mockAuthController);
		mockAccessRequirementDialog = mock(AccessRequirementDialog.class);

		licensedDownloader = new LicensedDownloader(mockView, mockAuthenticationController, mockGlobalApplicationState,
				mockSynapseJSNIUtils, mockGwt, mockAccessRequirementDialog);
		
		verify(mockView).setPresenter(licensedDownloader);
		
		
		// Parent Entity
		parentEntity = new Study();
		parentEntity.setId("StudyId");
		parentEntity.setUri("blahblah/Study/StudyId");

		// Entity
		entity = new FileEntity();
		entity.setDataFileHandleId("123");
		entity.setId("444");
		entity.setName("file entity");
		entity.setParentId(parentEntity.getId());
		
		FileHandle mockFileHandle = mock(FileHandle.class);
		when(mockFileHandle.getId()).thenReturn("123");
		
		entityBundle = new EntityBundle();
		entityBundle.setEntity(entity);
		entityBundle.setFileHandles(Collections.singletonList(mockFileHandle));
		
		// path for entity
		entityPath = new EntityPath();
		List<EntityHeader> path = new ArrayList<EntityHeader>();
		EntityHeader entityHeader = new EntityHeader();
		entityHeader.setId("root");
		entityHeader.setName("root");
		entityHeader.setType("/folder");
		path.add(entityHeader);
		entityHeader = new EntityHeader();
		entityHeader.setId("StudyId");
		entityHeader.setName("Study");
		entityHeader.setType("/Study");
		path.add(entityHeader);
		entityHeader = new EntityHeader();
		entityHeader.setId("layerId");
		entityHeader.setName("layer");
		entityHeader.setType("/layer");
		path.add(entityHeader);
		entityPath.setPath(path);
		
		when(mockSynapseJSNIUtils.getBaseFileHandleUrl()).thenReturn(baseFileHandleUrl);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testLoadFileDownloadUrl() throws RestServiceException {
		FileEntity entity = new FileEntity();
		entity.setId("myFileEntityId");
		entity.setVersionNumber(4l);
		resetMocks();
		entityBundle = new EntityBundle();
		entityBundle.setEntity(entity);
		// Null locations
		resetMocks();			
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		entityBundle.setFileHandles(null);		
		licensedDownloader.loadDownloadUrl(entityBundle);
		assertNull(licensedDownloader.getLoadedDirectDownloadURL());
		
		// Not Logged in Test: Download
		resetMocks();
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		licensedDownloader.loadDownloadUrl(entityBundle);
		assertNull(licensedDownloader.getLoadedDirectDownloadURL());
		
		// Success Test: Download
		resetMocks();			
		String fileHandleId = "22";
		S3FileHandle fileHandle = new S3FileHandle();
		fileHandle.setContentMd5("myContentMd5");
		fileHandle.setFileName("myFileName.png");
		fileHandle.setId(fileHandleId);
		List fileHandles = new ArrayList<FileHandle>();
		fileHandles.add(fileHandle);
		((FileEntity)entityBundle.getEntity()).setDataFileHandleId(fileHandleId);
		entityBundle.setFileHandles(fileHandles);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		licensedDownloader.loadDownloadUrl(entityBundle);
		assertNotNull(licensedDownloader.getLoadedDirectDownloadURL());
		
		// Success Test: External file
		resetMocks();			
		
		ExternalFileHandle externalFileHandle = new ExternalFileHandle();
		externalFileHandle.setFileName("myExternalFileName.png");
		externalFileHandle.setId(fileHandleId);
		externalFileHandle.setExternalURL("http://getbootstrap.com/javascript/");
		
		fileHandles = new ArrayList<FileHandle>();
		fileHandles.add(externalFileHandle);
		((FileEntity)entityBundle.getEntity()).setDataFileHandleId(fileHandleId);
		entityBundle.setFileHandles(fileHandles);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		licensedDownloader.loadDownloadUrl(entityBundle);
		assertNotNull(licensedDownloader.getLoadedDirectDownloadURL());
	}
	
	@Test
	public void testGetDirectDownloadUrlAvailable(){
		List<AccessRequirement> accessRequirements = new ArrayList<AccessRequirement>();
		licensedDownloader.setLicenseAgreement(accessRequirements, accessRequirements);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		licensedDownloader.loadDownloadUrl(entityBundle);
		//direct download available if there are no access requirements
		assertTrue(licensedDownloader.getDirectDownloadURL()!=null);
	}

	@Test
	public void testGetDirectDownloadUrlIsNull(){
		String touText = "some agreement";
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		List<AccessRequirement> accessRequirements = new ArrayList<AccessRequirement>();
		TermsOfUseAccessRequirement accessRequirement = new TermsOfUseAccessRequirement();
		accessRequirement.setTermsOfUse(touText);
		accessRequirements.add(accessRequirement);
		licensedDownloader.setLicenseAgreement(accessRequirements, accessRequirements);
		//direct download unavailable if there are any access requirements
		assertNull(licensedDownloader.getDirectDownloadURL());
		
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		//if not logged in, then it should also return null
		assertNull(licensedDownloader.getDirectDownloadURL());
	}
	
	/*
	 * Private methods
	 */
	private void resetMocks() {
		reset(mockView);
		reset(mockAuthenticationController);
		reset(mockGlobalApplicationState);
		reset(mockSynapseClient);
		reset(mockPlaceChanger);
		reset(mockStringCallback);
		jsonObjectAdapterProvider = new JSONObjectAdapterImpl();
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
	}	

	@Test
	public void testGetDirectDownloadUrlFileEntity() {
		String entityId = "syn99999";
		Long versionNumber = 8888L;
		FileEntity f = new FileEntity();
		FileHandle h = new S3FileHandle();
		f.setId(entityId);
		f.setVersionNumber(versionNumber);
		String downloadUrl = licensedDownloader.getDirectDownloadURL(f, h);
		assertTrue(downloadUrl.startsWith(baseFileHandleUrl));
		assertTrue(downloadUrl.contains(entityId));
		assertTrue(downloadUrl.contains(Long.toString(versionNumber)));
	}
	
	@Test
	public void testGetDirectDownloadUrlExternalFileEntity() {
		ExternalFileHandle h = new ExternalFileHandle();
		String url = "http://www.jhodgson.com/test.txt";
		h.setExternalURL(url);
		String downloadUrl = licensedDownloader.getDirectDownloadURL(new FileEntity(), h);
		assertEquals(url, downloadUrl);
	}
	
	@Test
	public void testGetDirectDownloadUrlSftpExternalFileEntity() {
		String sftpProxy = "http://mytestproxy.com/sftp";
		when(mockGlobalApplicationState.getSynapseProperty(WebConstants.SFTP_PROXY_ENDPOINT)).thenReturn(sftpProxy);
		
		ExternalFileHandle h = new ExternalFileHandle();
		String url = "sftp://www.jhodgson.com/test.txt";
		when(mockGwt.encodeQueryString(anyString())).thenReturn(url);
		h.setExternalURL(url);
		String downloadUrl = licensedDownloader.getDirectDownloadURL(new FileEntity(), h);
		
		assertTrue(downloadUrl.startsWith(sftpProxy));
		assertTrue(downloadUrl.contains(url));
	}
}
