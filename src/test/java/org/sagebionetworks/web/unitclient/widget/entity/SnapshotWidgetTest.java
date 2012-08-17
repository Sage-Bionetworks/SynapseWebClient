package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.Snapshot;
import org.sagebionetworks.repo.model.SnapshotGroup;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.SnapshotWidget;
import org.sagebionetworks.web.client.widget.entity.SnapshotWidgetView;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Unit test for the snapshot widget.
 * @author dburdick
 *
 */
@Ignore
public class SnapshotWidgetTest {

	AdapterFactory factory;
	GlobalApplicationState mockGlobal;
	AuthenticationController mockAuthenticationController;
	NodeModelCreator mockNodeModelCreator;
	PlaceChanger mockPlaceChanger;
	SynapseClientAsync mockSynapseClient;
	AutoGenFactory autoGenFactory;
	
	SnapshotWidget snapshotWidget;
	SnapshotWidgetView mockView;
	Snapshot snapshot;
	String testName = "testName";
	String testDesc = "testDesc";

	
	@Before
	public void before() throws JSONObjectAdapterException{		
		factory = new AdapterFactoryImpl();
		autoGenFactory = new AutoGenFactory();
		mockPlaceChanger = Mockito.mock(PlaceChanger.class);
		mockGlobal = Mockito.mock(GlobalApplicationState.class);
		when(mockGlobal.getPlaceChanger()).thenReturn(mockPlaceChanger);		
		mockAuthenticationController = Mockito.mock(AuthenticationController.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockView = mock(SnapshotWidgetView.class);
		
		snapshotWidget = new SnapshotWidget(factory, mockView, mockSynapseClient, mockNodeModelCreator, mockGlobal, mockAuthenticationController);
		snapshot = new Snapshot();
		snapshot.setId("syn1234");
	}
	
	@Test
	public void testSetSnapshot(){
		snapshotWidget.setSnapshot(snapshot, true, false);

		// assure that initial group is created if there are none
		assertEquals(1, snapshot.getGroups().size());		
		verify(mockView).setSnapshot(snapshot, true, false, true); // can edit

		// assure that only one inital group is added
		snapshotWidget.setSnapshot(snapshot, false, false);
		assertEquals(1, snapshot.getGroups().size());		
		verify(mockView).setSnapshot(snapshot, false, false, true); // can not edit		
	}
	
	@Test
	public void testAddGroup() throws Exception {		
		// read only
		snapshotWidget.setSnapshot(snapshot, false, true);		
		SnapshotGroup returnedGroup = snapshotWidget.addGroup("test", null);
		assertNull(returnedGroup);
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_IN_READ_ONLY_MODE);
		reset(mockView);
		
		// no edit permission
		snapshotWidget.setSnapshot(snapshot, false, false);		
		returnedGroup = snapshotWidget.addGroup("test", null);
		assertNull(returnedGroup);
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_NO_EDIT_PERMISSION);
		reset(mockView);
		
		snapshotWidget.setSnapshot(snapshot, true, false);		
		// null name
		returnedGroup = snapshotWidget.addGroup(null, null);
		assertNull(returnedGroup);
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_NAME_MUST_BE_DEFINED);
		reset(mockView);
		
		// empty name
		returnedGroup = snapshotWidget.addGroup("", null);
		assertNull(returnedGroup);
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_NAME_MUST_BE_DEFINED);
		reset(mockView);
		
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAddGroupSuccessBothDefined() throws Exception {		
		snapshotWidget.setSnapshot(snapshot, true, false);		

		// add name and description
		AsyncMockStubber.callSuccessWith("return json").when(mockSynapseClient).createOrUpdateEntity(eq(snapshot.getId()), anyString(), eq(false), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("return json").when(mockSynapseClient).getEntity(eq(snapshot.getId()), any(AsyncCallback.class));
		when(mockNodeModelCreator.createEntity(any(EntityWrapper.class))).thenReturn(snapshot);
		SnapshotGroup group = snapshotWidget.addGroup(testName, testDesc);
		assertNotNull(group);
		assertEquals(testName, snapshot.getGroups().get(1).getName());
		assertEquals(testDesc, snapshot.getGroups().get(1).getDescription());
		verify(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), eq(false), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString(), anyString());				
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testAddGroupSuccessOneDefined() {
		snapshotWidget.setSnapshot(snapshot, true, false);		
		
		// add name and null description
		AsyncMockStubber.callSuccessWith("return json").when(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), eq(false), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("return json").when(mockSynapseClient).getEntity(eq(snapshot.getId()), any(AsyncCallback.class));
		snapshotWidget.addGroup(testName, null);
		assertEquals(2, snapshot.getGroups().size());
		assertEquals(testName, snapshot.getGroups().get(1).getName());
		assertEquals(null, snapshot.getGroups().get(1).getDescription());
		verify(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), eq(false), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString(), anyString());		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testAddGroupFailure() {
		snapshotWidget.setSnapshot(snapshot, true, false);		

		// add name and description
		AsyncMockStubber.callFailureWith(null).when(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), eq(false), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("return json").when(mockSynapseClient).getEntity(eq(snapshot.getId()), any(AsyncCallback.class));
		snapshotWidget.addGroup(testName, testDesc);
		assertEquals(1, snapshot.getGroups().size()); // check backed out changes after failure
		verify(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), eq(false), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_FAILED_PERSIST);
	}

	
	
}
