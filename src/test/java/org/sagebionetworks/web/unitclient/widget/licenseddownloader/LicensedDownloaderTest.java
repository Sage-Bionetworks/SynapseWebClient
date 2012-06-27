package org.sagebionetworks.web.unitclient.widget.licenseddownloader;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.Study;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.EntitySchemaCacheImpl;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.licenseddownloader.LicensedDownloader;
import org.sagebionetworks.web.client.widget.licenseddownloader.LicensedDownloaderView;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.LicenseAgreement;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.users.UserData;
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
	Entity parentEntity;
	UserData user1;
	LicenseAgreement licenseAgreement;
	List<LocationData> locations;	
	EntityPath entityPath;
	EntityWrapper StudyEntityWrapper;
	EntityWrapper layerEntityWrapper;
	EntityWrapper pathEntityWrapper;
	
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
		String registryJson = SynapseClientImpl.getEntityTypeRegistryJson();
		AsyncMockStubber.callSuccessWith(registryJson).when(mockSynapseClient).getEntityTypeRegistryJSON(any(AsyncCallback.class));
		entityTypeProvider = new EntityTypeProvider(new RegisterConstantsStub(), new AdapterFactoryImpl(), new EntitySchemaCacheImpl(new AdapterFactoryImpl()));		

		
		licensedDownloader = new LicensedDownloader(mockView, mockAuthenticationController, mockGlobalApplicationState,
				jsonObjectAdapterProvider);
		
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
		user1 = new UserData("1", "email@email.com", "Username", "token", false);
		
		licenseAgreement = new LicenseAgreement();		
		licenseAgreement.setLicenseHtml("some agreement");

		// create a DownloadLocation model for this test
		LocationData downloadLocation = new LocationData();				
		downloadLocation.setPath("path");
		locations = new ArrayList<LocationData>();
		locations.add(downloadLocation);

		StudyEntityWrapper = new EntityWrapper("StudyEntityWrapper", Study.class.getName(), null);
		layerEntityWrapper = new EntityWrapper("layerEntityWrapper", Data.class.getName(), null);
		pathEntityWrapper = new EntityWrapper("pathEntityWrapper", EntityPath.class.getName(), null);
		
	}
	

	
	@SuppressWarnings("unchecked")
	@Test
	public void testLoadDownloadLocations() throws RestServiceException {
				
		// null model
		resetMocks();
		licensedDownloader.loadDownloadLocations(null, null);
		
		// Null locations
		resetMocks();			
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		((Locationable)entity).setLocations(null);		
		licensedDownloader.loadDownloadLocations(entity, false);
		verify(mockView).showDownloadsLoading();
		verify(mockView).setNoDownloads();	

		// Not Logged in Test: Download
		resetMocks();
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false); // not logged in
		entity.setLocations(locations);
		licensedDownloader.loadDownloadLocations(entity, true);
		verify(mockView).showDownloadsLoading();		
		verify(mockView).setNeedToLogIn();

		// Success Test: Download
		resetMocks();			
		entity.setLocations(locations);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		licensedDownloader.loadDownloadLocations(entity, true);
		verify(mockView).showDownloadsLoading();		
		verify(mockView).setDownloadLocations(locations, entity.getMd5());
	}

	@Test
	public void testAsWidget(){
		// make sure this version of asWidget can not be used		
		Widget widget = licensedDownloader.asWidget();
		assertNull(widget);		
	}
	
	@Test 
	public void testSetLicenseAgreement() {		
		// test license only
		licensedDownloader.setLicenseAgreement(licenseAgreement);				
		verify(mockView).setLicenseHtml(licenseAgreement.getLicenseHtml());
		
		reset(mockView);
		
		// test license and citation
		String citationHtml = "citation";
		licenseAgreement.setCitationHtml(citationHtml);
		licensedDownloader.setLicenseAgreement(licenseAgreement);
		verify(mockView).setCitationHtml(citationHtml);
		verify(mockView).setLicenseHtml(licenseAgreement.getLicenseHtml());		
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	@Test
	public void testSetLicenseAccepted() {
		// without callback
		licensedDownloader.setLicenseAcceptedCallback(null);

		licensedDownloader.setLicenseAccepted();		
		verify(mockView).setLicenceAcceptanceRequired(false);

		
		// with callback
		resetMocks();		
		AsyncCallback<Void> callback = mock(AsyncCallback.class);
		licensedDownloader.setLicenseAcceptedCallback(callback);

		licensedDownloader.setLicenseAccepted();		
		verify(callback).onSuccess(null);
		verify(mockView).setLicenceAcceptanceRequired(false);
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
