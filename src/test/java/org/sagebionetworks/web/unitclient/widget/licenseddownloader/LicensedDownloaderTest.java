package org.sagebionetworks.web.unitclient.widget.licenseddownloader;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.Study;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.EntitySchemaCacheImpl;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.JSONEntityFactory;
import org.sagebionetworks.web.client.transform.JSONEntityFactoryImpl;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.transform.NodeModelCreatorImpl;
import org.sagebionetworks.web.client.utils.APPROVAL_TYPE;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;
import org.sagebionetworks.web.client.widget.entity.JiraGovernanceConstants;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelperImpl;
import org.sagebionetworks.web.client.widget.licenseddownloader.LicensedDownloader;
import org.sagebionetworks.web.client.widget.licenseddownloader.LicensedDownloaderView;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.RegisterConstantsStub;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class LicensedDownloaderTest {
		
	LicensedDownloader licensedDownloader;
	LicensedDownloaderView mockView;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	SynapseClientAsync mockSynapseClient;
	PlaceChanger mockPlaceChanger;
	AsyncCallback<String> mockStringCallback;
	
	JSONObjectAdapter jsonObjectAdapterProvider;
	EntityTypeProvider entityTypeProvider;
	Locationable entity;
	EntityBundle entityBundle;
	Entity parentEntity;
	UserSessionData user1;
	List<LocationData> locations;	
	EntityPath entityPath;
	EntityWrapper StudyEntityWrapper;
	EntityWrapper layerEntityWrapper;
	EntityWrapper pathEntityWrapper;
	JiraURLHelper jiraURLHelper;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setup() throws UnsupportedEncodingException, JSONObjectAdapterException{		
		mockView = Mockito.mock(LicensedDownloaderView.class);		
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		jsonObjectAdapterProvider = new JSONObjectAdapterImpl();
		mockStringCallback = mock(AsyncCallback.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);


		// create entity type provider
		entityTypeProvider = new EntityTypeProvider(new RegisterConstantsStub(), new AdapterFactoryImpl(), new EntitySchemaCacheImpl(new AdapterFactoryImpl()));		

		AdapterFactory adapterFactory = new AdapterFactoryImpl();
		JSONEntityFactory factory = new JSONEntityFactoryImpl(adapterFactory);
		NodeModelCreator nodeModelCreator = new NodeModelCreatorImpl(factory, adapterFactory.createNew());
		
		JiraGovernanceConstants gc = mock(JiraGovernanceConstants.class);

		jiraURLHelper  = new JiraURLHelperImpl(gc);

		licensedDownloader = new LicensedDownloader(mockView, mockAuthenticationController, mockGlobalApplicationState,
				jsonObjectAdapterProvider, mockSynapseClient, jiraURLHelper, nodeModelCreator);
		
		verify(mockView).setPresenter(licensedDownloader);
		
		
		// Parent Entity
		parentEntity = new Study();
		parentEntity.setId("StudyId");
		parentEntity.setUri("blahblah/Study/StudyId");

		// Entity
		String md5sum = "4759818803f93967eb250f784cf8576d";
		String contentType = "application/jpg";		
		entity = new Data();
		entity.setUri("blahblah/layer/layerId");
		entity.setId("layerId");
		entity.setName("layer");
		entity.setParentId(parentEntity.getId());
		entity.setMd5(md5sum);
		entity.setContentType(contentType);
		
		entityBundle = new EntityBundle(entity, null, null, null, null, null, null, null);
		
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
		
		// User
		user1 = new UserSessionData();
		UserProfile profile = new UserProfile();
		profile.setDisplayName("Display Name");
		profile.setOwnerId("1");
		user1.setProfile(profile);
		user1.setSessionToken("token");
		user1.setIsSSO(false);

		licensedDownloader.setUserProfile(profile);
		
		// create a DownloadLocation model for this test
		LocationData downloadLocation = new LocationData();				
		downloadLocation.setPath("path");
		locations = new ArrayList<LocationData>();
		locations.add(downloadLocation);

		StudyEntityWrapper = new EntityWrapper("StudyEntityWrapper", Study.class.getName());
		layerEntityWrapper = new EntityWrapper("layerEntityWrapper", Data.class.getName());
		pathEntityWrapper = new EntityWrapper("pathEntityWrapper", EntityPath.class.getName());
		
		when(mockView.getDirectDownloadURL()).thenReturn("http://synapse.sagebase.org/file.png");
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testLoadDownloadLocations() throws RestServiceException {
				
		// null model
		resetMocks();
		licensedDownloader.loadDownloadUrl(null);
		
		// Null locations
		resetMocks();			
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		((Locationable)entity).setLocations(null);		
		licensedDownloader.loadDownloadUrl(entityBundle);
		verify(mockView).showDownloadsLoading();
		verify(mockView).setNoDownloads();	

		// Not Logged in Test: Download
		resetMocks();
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false); // not logged in
		entity.setLocations(locations);
		licensedDownloader.loadDownloadUrl(entityBundle);
		verify(mockView).showDownloadsLoading();		
		verify(mockView).setNeedToLogIn();

		// Success Test: Download
		resetMocks();			
		entity.setLocations(locations);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		licensedDownloader.loadDownloadUrl(entityBundle);
		verify(mockView).showDownloadsLoading();		
		verify(mockView).setDownloadLocations(locations, entity.getMd5());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testLoadFileDownloadUrl() throws RestServiceException {
		FileEntity entity = new FileEntity();
		entity.setId("myFileEntityId");
		entity.setVersionNumber(4l);
		resetMocks();
		entityBundle = new EntityBundle(entity, null, null, null, null, null, null, null);
		
		// Null locations
		resetMocks();			
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		entityBundle.setFileHandles(null);		
		licensedDownloader.loadDownloadUrl(entityBundle);
		verify(mockView).showDownloadsLoading();
		verify(mockView).setNoDownloads();	

		// Not Logged in Test: Download
		resetMocks();
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false); // not logged in
		licensedDownloader.loadDownloadUrl(entityBundle);
		verify(mockView).showDownloadsLoading();		
		verify(mockView).setNeedToLogIn();

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
		verify(mockView).showDownloadsLoading();		
		verify(mockView).setDownloadLocation(fileHandle.getFileName(), entity.getId(), entity.getVersionNumber(), fileHandle.getContentMd5());
	}


	@Test
	public void testAsWidget(){
		// make sure this version of asWidget can not be used		
		Widget widget = licensedDownloader.asWidget();
		assertNull(widget);		
	}
	
	@Test 
	public void testSetLicenseAgreement() {	
		String touText = "some agreement";
		List<AccessRequirement> accessRequirements = new ArrayList<AccessRequirement>();
		TermsOfUseAccessRequirement accessRequirement = new TermsOfUseAccessRequirement();
		accessRequirement.setTermsOfUse(touText);
		accessRequirements.add(accessRequirement);

		licensedDownloader.setLicenseAgreement(accessRequirements, accessRequirements);				
		verify(mockView).setLicenseHtml(touText);
		
		reset(mockView);
		
		ACTAccessRequirement actAR = new ACTAccessRequirement();
		String actContactInfo = "act contact info";
		actAR.setActContactInfo(actContactInfo);
		accessRequirements.add(actAR);
		List<AccessRequirement> unmetAccessRequirements = new ArrayList<AccessRequirement>();
		unmetAccessRequirements.add(actAR);
		licensedDownloader.setLicenseAgreement(accessRequirements, unmetAccessRequirements);				
		verify(mockView).setLicenseHtml(actContactInfo);
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	@Test
	public void testSetLicenseAccepted() {
		String touText = "some agreement";
		List<AccessRequirement> accessRequirements = new ArrayList<AccessRequirement>();
		TermsOfUseAccessRequirement accessRequirement = new TermsOfUseAccessRequirement();
		accessRequirement.setTermsOfUse(touText);
		accessRequirements.add(accessRequirement);

		licensedDownloader.setLicenseAgreement(accessRequirements, accessRequirements);
		licensedDownloader.setLicenseAccepted();		
		verify(mockView).setApprovalType(APPROVAL_TYPE.USER_AGREEMENT);
		verify(mockView).setRestrictionLevel(RESTRICTION_LEVEL.RESTRICTED);

		// with callback
		resetMocks();		
	
		licensedDownloader.setLicenseAccepted();		
	}

	@Test
	public void testGetDirectDownloadUrlAvailable(){
		List<AccessRequirement> accessRequirements = new ArrayList<AccessRequirement>();
		licensedDownloader.setLicenseAgreement(accessRequirements, accessRequirements);
		//direct download available if there are no access requirements
		assertTrue(licensedDownloader.getDirectDownloadURL()!=null);
	}

	@Test
	public void testGetDirectDownloadUrlIsNull(){
		String touText = "some agreement";
		List<AccessRequirement> accessRequirements = new ArrayList<AccessRequirement>();
		TermsOfUseAccessRequirement accessRequirement = new TermsOfUseAccessRequirement();
		accessRequirement.setTermsOfUse(touText);
		accessRequirements.add(accessRequirement);
		licensedDownloader.setLicenseAgreement(accessRequirements, accessRequirements);
		//direct download unavailable if there are any access requirements
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

	private void configureTestLoadMocks() throws Exception {
		when(mockAuthenticationController.getLoggedInUser()).thenReturn(user1);
		AsyncMockStubber.callSuccessWith(StudyEntityWrapper).when(mockSynapseClient).getEntity(eq(parentEntity.getId()), any(AsyncCallback.class)); 
		AsyncMockStubber.callSuccessWith(layerEntityWrapper).when(mockSynapseClient).getEntity(eq(entity.getId()), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(pathEntityWrapper).when(mockSynapseClient).getEntityPath(eq(entity.getId()), any(AsyncCallback.class));
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
	}
	

	private void configureTestFindEulaIdMocks() throws Exception {
		when(mockAuthenticationController.getLoggedInUser()).thenReturn(user1);
		AsyncMockStubber.callSuccessWith(StudyEntityWrapper).when(mockSynapseClient).getEntity(eq(parentEntity.getId()), any(AsyncCallback.class)); 
		AsyncMockStubber.callSuccessWith(layerEntityWrapper).when(mockSynapseClient).getEntity(eq(entity.getId()), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(pathEntityWrapper).when(mockSynapseClient).getEntityPath(eq(entity.getId()), any(AsyncCallback.class));
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);

	}
	
	
	
	
}
