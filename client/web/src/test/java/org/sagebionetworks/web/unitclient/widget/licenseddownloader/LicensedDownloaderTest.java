package org.sagebionetworks.web.unitclient.widget.licenseddownloader;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.Dataset;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.Eula;
import org.sagebionetworks.repo.model.Layer;
import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.services.NodeServiceAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.licenseddownloader.LicenceServiceAsync;
import org.sagebionetworks.web.client.widget.licenseddownloader.LicensedDownloader;
import org.sagebionetworks.web.client.widget.licenseddownloader.LicensedDownloaderView;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.LicenseAgreement;
import org.sagebionetworks.web.shared.NodeType;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.users.UserData;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class LicensedDownloaderTest {
		
	LicensedDownloader licensedDownloader;
	LicensedDownloaderView mockView;
	NodeServiceAsync mockNodeService;
	LicenceServiceAsync mockLicenseService;
	NodeModelCreator mockNodeModelCreator;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	SynapseClientAsync mockSynapseClient;
	PlaceChanger mockPlaceChanger;
	AsyncCallback<String> mockStringCallback;
	
	JSONObjectAdapter jsonObjectAdapterProvider;
	EntityTypeProvider entityTypeProvider;
	Locationable entity;
	Entity parentEntity;
	Eula eula1;
	UserData user1;
	LicenseAgreement licenseAgreement;
	List<LocationData> locations;	
	EntityPath entityPath;
	EntityWrapper datasetEntityWrapper;
	EntityWrapper layerEntityWrapper;
	EntityWrapper pathEntityWrapper;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setup(){		
		mockView = Mockito.mock(LicensedDownloaderView.class);		
		mockNodeService = mock(NodeServiceAsync.class);
		mockLicenseService = mock(LicenceServiceAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		jsonObjectAdapterProvider = new JSONObjectAdapterImpl();
		mockStringCallback = mock(AsyncCallback.class);


		// create entity type provider
		String registryJson = SynapseClientImpl.getEntityTypeRegistryJson();
		AsyncMockStubber.callSuccessWith(registryJson).when(mockSynapseClient).getEntityTypeRegistryJSON(any(AsyncCallback.class));
		entityTypeProvider = new EntityTypeProvider(mockSynapseClient, new JSONObjectAdapterImpl());		

		
		licensedDownloader = new LicensedDownloader(mockView, mockNodeService,
				mockLicenseService, mockNodeModelCreator,
				mockAuthenticationController, mockGlobalApplicationState,
				mockSynapseClient, jsonObjectAdapterProvider, entityTypeProvider);
		
		
		licensedDownloader.setPlaceChanger(mockPlaceChanger);
		verify(mockView).setPresenter(licensedDownloader);
		
		// Eula	
		eula1 = new Eula();
		eula1.setId("eulaId");
		eula1.setAgreement("Agreement");
		eula1.setName("Agreement 1");
		
		// Parent Entity
		parentEntity = new Dataset();
		parentEntity.setId("datasetId");
		parentEntity.setUri("blahblah/dataset/datasetId");
		((Dataset)parentEntity).setEulaId(eula1.getId());

		// Entity
		String md5sum = "4759818803f93967eb250f784cf8576d";
		String contentType = "application/jpg";		
		entity = new Layer();
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
		entityHeader.setId("datasetId");
		entityHeader.setName("dataset");
		entityHeader.setType("/dataset");
		path.add(entityHeader);
		entityHeader = new EntityHeader();
		entityHeader.setId("layerId");
		entityHeader.setName("layer");
		entityHeader.setType("/layer");
		path.add(entityHeader);
		entityPath.setPath(path);
		
		// User
		user1 = new UserData("email@email.com", "Username", "token", false);
		
		licenseAgreement = new LicenseAgreement();		
		licenseAgreement.setLicenseHtml(eula1.getAgreement());

		// create a DownloadLocation model for this test
		LocationData downloadLocation = new LocationData();				
		downloadLocation.setPath("path");
		locations = new ArrayList<LocationData>();
		locations.add(downloadLocation);

		datasetEntityWrapper = new EntityWrapper("datasetEntityWrapper", Dataset.class.getName(), null);
		layerEntityWrapper = new EntityWrapper("layerEntityWrapper", Layer.class.getName(), null);
		pathEntityWrapper = new EntityWrapper("pathEntityWrapper", EntityPath.class.getName(), null);
		
	}
	

	@SuppressWarnings("unchecked")
	@Test
	public void testFindEulaId() throws Exception {
		// test for no eula in parent path
		resetMocks();
		configureTestFindEulaIdMocks();
		((Dataset)parentEntity).setEulaId(null);		
		licensedDownloader.findEulaId(entity, mockStringCallback);
		verify(mockStringCallback).onSuccess(null);

		// test failure for 1st parent (dataset) service retrieval
		resetMocks();
		configureTestFindEulaIdMocks();
		AsyncMockStubber.callFailureWith(new Throwable()).when(mockSynapseClient).getEntity(eq(parentEntity.getId()), any(AsyncCallback.class)); 				
		licensedDownloader.findEulaId(entity, mockStringCallback);
		verify(mockStringCallback).onFailure(any(Throwable.class));
		
		// test failure for 1st parent (dataset) model creation
		resetMocks();
		configureTestFindEulaIdMocks();
		when(mockNodeModelCreator.createEntity(datasetEntityWrapper)).thenReturn(null); 				
		licensedDownloader.findEulaId(entity, mockStringCallback);
		verify(mockStringCallback).onFailure(any(Throwable.class));

		// test finding eula Id in 1st parent (dataset)
		resetMocks();
		configureTestFindEulaIdMocks();
		String eulaId = "eulaId";
		((Dataset)parentEntity).setEulaId(eulaId);		
		licensedDownloader.findEulaId(entity, mockStringCallback);
		verify(mockStringCallback).onSuccess(eulaId);
		
	}

	
	@SuppressWarnings("unchecked")
	@Test
	public void testLoadLicenseAgreement() throws Exception {
		LicenseAgreement licenseAgreement = new LicenseAgreement(eula1.getAgreement(), null, eula1.getId());
		
		// null model
		resetMocks();
		licensedDownloader.loadLicenseAgreement(null);		
		
		// Failure to get parent via synapse client		
		resetMocks();
		configureTestLoadMocks();
		AsyncMockStubber.callFailureWith(new Throwable("error message")).when(mockSynapseClient).getEntity(eq(parentEntity.getId()), any(AsyncCallback.class)); // fail for get parent
		licensedDownloader.loadLicenseAgreement(entity);
		verify(mockView).showDownloadFailure();

		// Success of parental get but failure in Entity creation 
		resetMocks();
		configureTestLoadMocks();
		when(mockNodeModelCreator.createEntity(any(EntityWrapper.class))).thenReturn(null); // failed entity creation
		licensedDownloader.loadLicenseAgreement(entity);
		verify(mockView).showDownloadFailure();

		// Dataset with null EULA 
		resetMocks();
		configureTestLoadMocks();
		((Dataset)parentEntity).setEulaId(null); // null EULA
		licensedDownloader.loadLicenseAgreement(entity);
		verify(mockView).setLicenceAcceptanceRequired(false);		
		
		// model creation of EULA fails
		resetMocks();
		configureTestLoadMocks(); 
		when(mockNodeModelCreator.createEntity(anyString(), eq(Eula.class))).thenReturn(null); // null EULA object
		licensedDownloader.loadLicenseAgreement(entity);
		verify(mockView).showDownloadFailure();
	
		// License Acceptance failure
		resetMocks();
		configureTestLoadMocks();
		AsyncMockStubber.callFailureWith(new Throwable("error message")).when(mockLicenseService).hasAccepted(eq(user1.getEmail()), eq(eula1.getId()), eq(parentEntity.getId()), any(AsyncCallback.class)); // has not accepted
		licensedDownloader.loadLicenseAgreement(entity);
		verify(mockView).showDownloadFailure();

		// Check all okay but with user not logged in and trying to show download
		resetMocks();
		configureTestLoadMocks();
		when(mockAuthenticationController.getLoggedInUser()).thenReturn(null); // null user
		licensedDownloader.loadLicenseAgreement(entity);
		verify(mockView).setUnauthorizedDownloads();		
		
		// License Accepted == False
		resetMocks();
		configureTestLoadMocks();
		AsyncMockStubber.callSuccessWith(false).when(mockLicenseService).hasAccepted(eq(user1.getEmail()), eq(eula1.getId()), eq(parentEntity.getId()), any(AsyncCallback.class)); // accepted == false
		licensedDownloader.loadLicenseAgreement(entity);
		verify(mockView).setLicenceAcceptanceRequired(true);
		verify(mockView).setLicenseHtml(licenseAgreement.getLicenseHtml());

		
		// License Accepted == True
		resetMocks();
		configureTestLoadMocks();
		AsyncMockStubber.callSuccessWith(true).when(mockLicenseService).hasAccepted(eq(user1.getEmail()), eq(eula1.getId()), eq(parentEntity.getId()), any(AsyncCallback.class)); // accepted == true
		licensedDownloader.loadLicenseAgreement(entity);
		verify(mockView).setLicenceAcceptanceRequired(false);
		verify(mockView).setLicenseHtml(licenseAgreement.getLicenseHtml());
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testLoadDownloadLocations() throws RestServiceException {
				
		// null model
		resetMocks();
		licensedDownloader.loadDownloadLocations(null, null);
		
		// Null locations
		resetMocks();			
		when(mockAuthenticationController.getLoggedInUser()).thenReturn(user1);
		((Locationable)entity).setLocations(null);		
		licensedDownloader.loadDownloadLocations(entity, false);
		verify(mockView).showDownloadsLoading();
		verify(mockView).setNoDownloads();	
								
		// Success Test: No download
		resetMocks();
		when(mockAuthenticationController.getLoggedInUser()).thenReturn(user1);
		entity.setLocations(locations);		
		licensedDownloader.loadDownloadLocations(entity, false);
		verify(mockView).showDownloadsLoading();		
		verify(mockView).setDownloadLocations(locations, entity.getMd5());

		// Not Logged in Test: Download
		resetMocks();			
		when(mockAuthenticationController.getLoggedInUser()).thenReturn(null); // not logged in
		entity.setLocations(locations);
		licensedDownloader.loadDownloadLocations(entity, true);
		verify(mockView).showDownloadsLoading();		
		verify(mockView).setDownloadLocations(locations, entity.getMd5());
		verify(mockView).showInfo(anyString(), anyString());
		verify(mockPlaceChanger).goTo(any(LoginPlace.class));

		// Success Test: Download
		resetMocks();			
		entity.setLocations(locations);
		when(mockAuthenticationController.getLoggedInUser()).thenReturn(user1);
		licensedDownloader.loadDownloadLocations(entity, true);
		verify(mockView).showDownloadsLoading();		
		verify(mockView).setDownloadLocations(locations, entity.getMd5());
		verify(mockView).showWindow();
	}

	@Test
	public void testAsWidgetParameterized() throws Exception {

		// License Accepted == True
		resetMocks();
		configureTestLoadMocks();

		// Success Test: No download
		entity.setLocations(locations);		
		
		licensedDownloader.asWidget(entity, false);		

		verify(mockView).showDownloadsLoading();		
		verify(mockView).setDownloadLocations(locations, entity.getMd5());
		verify(mockView).setLicenceAcceptanceRequired(false);
		verify(mockView).setLicenseHtml(licenseAgreement.getLicenseHtml());

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
		reset(mockNodeService);
		reset(mockLicenseService);
		reset(mockNodeModelCreator);
		reset(mockAuthenticationController);
		reset(mockGlobalApplicationState);
		reset(mockSynapseClient);
		reset(mockPlaceChanger);
		reset(mockStringCallback);
		jsonObjectAdapterProvider = new JSONObjectAdapterImpl();
	}	

	private void configureTestLoadMocks() throws Exception {
		((Dataset)parentEntity).setEulaId(eula1.getId());
		when(mockAuthenticationController.getLoggedInUser()).thenReturn(user1);
		when(mockNodeModelCreator.createEntity(datasetEntityWrapper)).thenReturn(parentEntity);
		when(mockNodeModelCreator.createEntity(layerEntityWrapper)).thenReturn(entity);
		when(mockNodeModelCreator.createEntity(anyString(), eq(Eula.class))).thenReturn(eula1);
		when(mockNodeModelCreator.createEntity(pathEntityWrapper, EntityPath.class)).thenReturn(entityPath);
		AsyncMockStubber.callSuccessWith(datasetEntityWrapper).when(mockSynapseClient).getEntity(eq(parentEntity.getId()), any(AsyncCallback.class)); 
		AsyncMockStubber.callSuccessWith(layerEntityWrapper).when(mockSynapseClient).getEntity(eq(entity.getId()), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(pathEntityWrapper).when(mockSynapseClient).getEntityPath(eq(entity.getId()), anyString(), any(AsyncCallback.class)); 		
		AsyncMockStubber.callSuccessWith("eula json").when(mockNodeService).getNodeJSON(eq(NodeType.EULA), eq(eula1.getId()), any(AsyncCallback.class));		
		AsyncMockStubber.callSuccessWith(true).when(mockLicenseService).hasAccepted(eq(user1.getEmail()), eq(eula1.getId()), eq(parentEntity.getId()), any(AsyncCallback.class));
	}

	private void configureTestFindEulaIdMocks() throws Exception {
		((Dataset)parentEntity).setEulaId(eula1.getId());
		when(mockAuthenticationController.getLoggedInUser()).thenReturn(user1);
		when(mockNodeModelCreator.createEntity(datasetEntityWrapper)).thenReturn(parentEntity);
		when(mockNodeModelCreator.createEntity(layerEntityWrapper)).thenReturn(entity);
		when(mockNodeModelCreator.createEntity(pathEntityWrapper, EntityPath.class)).thenReturn(entityPath);
		AsyncMockStubber.callSuccessWith(datasetEntityWrapper).when(mockSynapseClient).getEntity(eq(parentEntity.getId()), any(AsyncCallback.class)); 
		AsyncMockStubber.callSuccessWith(layerEntityWrapper).when(mockSynapseClient).getEntity(eq(entity.getId()), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(pathEntityWrapper).when(mockSynapseClient).getEntityPath(eq(entity.getId()), anyString(), any(AsyncCallback.class)); 				
	}
	
	
	
}
