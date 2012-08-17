package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.*;
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

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Snapshot;
import org.sagebionetworks.repo.model.SnapshotGroup;
import org.sagebionetworks.repo.model.SnapshotGroupRecord;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.SnapshotGroupRecordDisplay;
import org.sagebionetworks.web.client.widget.entity.SnapshotWidget;
import org.sagebionetworks.web.client.widget.entity.SnapshotWidgetView;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Unit test for the snapshot widget.
 * @author dburdick
 *
 */
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
	final boolean CAN_EDIT = true;
	final boolean READ_ONLY = false;
	
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
		snapshot = createDefaultSnapshot();
	}
	
	@Test
	public void testSetSnapshot(){
		snapshotWidget.setSnapshot(null, CAN_EDIT, READ_ONLY);
		verify(mockView).setSnapshot(null, CAN_EDIT, READ_ONLY);
		
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);

		// assure that initial group is created if there are none
		assertEquals(1, snapshot.getGroups().size());		
		verify(mockView).setSnapshot(snapshot, CAN_EDIT, READ_ONLY); // can edit

		// assure that only one inital group is added
		snapshotWidget.setSnapshot(snapshot, false, false);
		assertEquals(1, snapshot.getGroups().size());		
		verify(mockView).setSnapshot(snapshot, false, false); // can not edit		
	}
	
	@Test
	public void testLoadRowDetails() {
		// does nothing
		snapshotWidget.setSnapshot(null, CAN_EDIT, READ_ONLY);
		snapshotWidget.loadRowDetails();
		
		// empty snapshot 
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);
		snapshotWidget.loadRowDetails();		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testLoadRowDetailsSuccessVersion() throws Exception {
		// load one row
		String targetId = "syn321";
		Long targetVersionNumber = new Long(2);
		addSingleGroupRecordToSnapshot(snapshot, targetId, targetVersionNumber, null);
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);
		Data rowData = getDataEntity(targetId, targetVersionNumber);
		EntityWrapper expectedWrapper =  new EntityWrapper(rowData.writeToJSONObject(factory.createNew()).toJSONString(), Data.class.getName(), null);
		AsyncMockStubber.callSuccessWith(expectedWrapper).when(mockSynapseClient).getEntityForVersion(eq(targetId), eq(targetVersionNumber), any(AsyncCallback.class));
		when(mockNodeModelCreator.createEntity(expectedWrapper)).thenReturn(rowData);
		
		snapshotWidget.loadRowDetails();

		verify(mockView).setSnapshotGroupRecordDisplay(eq(0), eq(0), any(SnapshotGroupRecordDisplay.class)); 
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testLoadRowDetailsSuccessNoVersion() throws Exception {
		// load one row
		String targetId = "syn321";
		Long targetVersionNumber = null;
		addSingleGroupRecordToSnapshot(snapshot, targetId, targetVersionNumber, null);
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);
		Data rowData = getDataEntity(targetId, targetVersionNumber);
		EntityWrapper expectedWrapper =  new EntityWrapper(rowData.writeToJSONObject(factory.createNew()).toJSONString(), Data.class.getName(), null);
		AsyncMockStubber.callSuccessWith(expectedWrapper).when(mockSynapseClient).getEntity(eq(targetId), any(AsyncCallback.class));
		when(mockNodeModelCreator.createEntity(expectedWrapper)).thenReturn(rowData);
		
		snapshotWidget.loadRowDetails();

		verify(mockView).setSnapshotGroupRecordDisplay(eq(0), eq(0), any(SnapshotGroupRecordDisplay.class)); 
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testLoadRowDetailsFailureUnauthz() throws Exception {
		// load one row
		String targetId = "syn321";
		Long targetVersionNumber = new Long(2);
		addSingleGroupRecordToSnapshot(snapshot, targetId, targetVersionNumber, null);
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);
		UnauthorizedException unauth = new UnauthorizedException();
		AsyncMockStubber.callFailureWith(unauth).when(mockSynapseClient).getEntityForVersion(eq(targetId), eq(targetVersionNumber), any(AsyncCallback.class));
		
		snapshotWidget.loadRowDetails();

		verify(mockView).setSnapshotGroupRecordDisplay(eq(0), eq(0), any(SnapshotGroupRecordDisplay.class)); 
	}	
	
	@SuppressWarnings("unchecked")
	@Test
	public void testLoadRowDetailsFailureOther() throws Exception {
		// load one row
		String targetId = "syn321";
		Long targetVersionNumber = new Long(2);
		addSingleGroupRecordToSnapshot(snapshot, targetId, targetVersionNumber, null);
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);
		Exception exception = new Exception();
		AsyncMockStubber.callFailureWith(exception).when(mockSynapseClient).getEntityForVersion(eq(targetId), eq(targetVersionNumber), any(AsyncCallback.class));
		
		snapshotWidget.loadRowDetails();

		verify(mockView).showErrorMessage(DisplayConstants.ERROR_FAILED_PERSIST); 
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
		
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);		
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
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);		
		// setup - add name and description
		String expectedJson = setupAddGroup(testName, testDesc);
		SnapshotGroup group = snapshotWidget.addGroup(testName, testDesc);

		Snapshot actualSnapshot = snapshotWidget.getSnapshot();
		
		// verify service calls
		verify(mockSynapseClient).updateEntity(eq(expectedJson), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString(), anyString());				
		
		// validate returned group
		assertNotNull(group);
		assertEquals(testName, group.getName());
		assertEquals(testDesc, group.getDescription());
		assertNotNull(group.getRecords());

		// validate change to model
		assertEquals(2, actualSnapshot.getGroups().size());
		assertEquals(testName, actualSnapshot.getGroups().get(1).getName());
		assertEquals(testDesc, actualSnapshot.getGroups().get(1).getDescription());
		assertNotNull(actualSnapshot.getGroups().get(1).getRecords());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testAddGroupSuccessOneDefined() throws Exception {		
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);		
		// setup - add name and null description
		String expectedJson = setupAddGroup(testName, null);
		SnapshotGroup group = snapshotWidget.addGroup(testName, null);

		Snapshot actualSnapshot = snapshotWidget.getSnapshot();
		
		// verify service calls
		verify(mockSynapseClient).updateEntity(eq(expectedJson), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString(), anyString());				
		
		// validate returned group
		assertNotNull(group);
		assertEquals(testName, group.getName());
		assertNull(group.getDescription());
		assertNotNull(group.getRecords());

		// validate change to model 
		assertEquals(2, actualSnapshot.getGroups().size());
		assertEquals(testName, actualSnapshot.getGroups().get(1).getName());
		assertNull(testDesc, actualSnapshot.getGroups().get(1).getDescription());
		assertNotNull(actualSnapshot.getGroups().get(1).getRecords());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testAddGroupFailure() throws Exception {
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);		

		// setup - fail update add name and description
		setupAddGroup(testName, testDesc);
		AsyncMockStubber.callFailureWith(null).when(mockSynapseClient).updateEntity(anyString(), any(AsyncCallback.class));
		Snapshot rebuildSnapshot = getSetSnapshotState();
		setupRebuildEverythingCallbacks(rebuildSnapshot);
		snapshotWidget.addGroup(testName, testDesc);
		
		Snapshot actualSnapshot = snapshotWidget.getSnapshot();
		
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_FAILED_PERSIST);
		verify(mockSynapseClient).updateEntity(anyString(), any(AsyncCallback.class));
		verifyRebuildEverything(rebuildSnapshot, actualSnapshot, 1);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateGroup() throws Exception {		
		// read only
		snapshotWidget.setSnapshot(snapshot, false, true);		
		snapshotWidget.updateGroup(0, "test", null);		
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_IN_READ_ONLY_MODE);
		reset(mockView);
		
		// no edit permission
		snapshotWidget.setSnapshot(snapshot, false, false);		
		snapshotWidget.updateGroup(0, "test", null);		
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_NO_EDIT_PERMISSION);
		reset(mockView);
		
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);		
		// null name
		snapshotWidget.updateGroup(0, null, null);		
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_NAME_MUST_BE_DEFINED);
		reset(mockView);
		
		// empty name
		snapshotWidget.updateGroup(0, "", null);		
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_NAME_MUST_BE_DEFINED);
		reset(mockView);
		
	}
	
	@Test
	public void testUpdateGroupGroupIdx() throws Exception {
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);
		reset(mockView);
		
		// group index out of range
		Snapshot rebuildSnapshot = getSetSnapshotState();
		setupRebuildEverythingCallbacks(rebuildSnapshot);
		snapshotWidget.updateGroup(1, "test", null);		
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_GENERIC);
		verifyRebuildEverything(rebuildSnapshot, snapshotWidget.getSnapshot(), 1);
		
	}
	

	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateGroupSuccessBothDefined() throws Exception {		
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);
		reset(mockView);
		Snapshot expectedSnapshot = getSetSnapshotState();
		expectedSnapshot.getGroups().get(0).setName(testName);
		expectedSnapshot.getGroups().get(0).setDescription(testDesc);	
		setupUpdateEntityWhens(expectedSnapshot);
		
		int groupIndex = 0;
		snapshotWidget.updateGroup(groupIndex, testName, testDesc);

		// verify service calls
		verify(mockSynapseClient).updateEntity(anyString(), any(AsyncCallback.class));
		verify(mockView).showInfo(eq(DisplayConstants.UPDATE_SAVED), anyString());				
		
		// validate change to model
		Snapshot actualSnapshot = snapshotWidget.getSnapshot();
		assertEquals(1, actualSnapshot.getGroups().size());
		assertEquals(testName, actualSnapshot.getGroups().get(0).getName());
		assertEquals(testDesc, actualSnapshot.getGroups().get(0).getDescription());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateGroupFailure() throws Exception {		
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);
		reset(mockView);
		Snapshot expectedSnapshot = getSetSnapshotState();
		expectedSnapshot.getGroups().get(0).setName(testName);
		expectedSnapshot.getGroups().get(0).setDescription(testDesc);	
		setupUpdateEntityWhens(expectedSnapshot);
		AsyncMockStubber.callFailureWith(null).when(mockSynapseClient).updateEntity(anyString(), any(AsyncCallback.class));
		
		Snapshot rebuildSnapshot = getSetSnapshotState();
		setupRebuildEverythingCallbacks(rebuildSnapshot);
		int groupIndex = 0;
		snapshotWidget.updateGroup(groupIndex, testName, testDesc);
		
		// verify failure
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_FAILED_PERSIST);
		verifyRebuildEverything(rebuildSnapshot, snapshotWidget.getSnapshot(), 1);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRemoveGroup() throws Exception {		
		// read only
		snapshotWidget.setSnapshot(snapshot, false, true);		
		snapshotWidget.removeGroup(0);		
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_IN_READ_ONLY_MODE);
		reset(mockView);
		
		// no edit permission
		snapshotWidget.setSnapshot(snapshot, false, false);		
		snapshotWidget.removeGroup(0);		
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_NO_EDIT_PERMISSION);
		reset(mockView);		
	}
	
	@Test
	public void testRemoveGroupGroupIdx() throws Exception {
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);
		reset(mockView);
		
		// group index out of range
		Snapshot rebuildSnapshot = getSetSnapshotState();
		setupRebuildEverythingCallbacks(rebuildSnapshot);
		snapshotWidget.removeGroup(1);		
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_GENERIC);
		verifyRebuildEverything(rebuildSnapshot, snapshotWidget.getSnapshot(), 1);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRemoveGroupSuccess() throws Exception {		
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);
		reset(mockView);
		Snapshot expectedSnapshot = getSetSnapshotState();
		expectedSnapshot.getGroups().remove(0);
		setupUpdateEntityWhens(expectedSnapshot);
		
		snapshotWidget.removeGroup(0);

		// verify service calls
		verify(mockSynapseClient).updateEntity(anyString(), any(AsyncCallback.class));
		verify(mockView).showInfo(eq(DisplayConstants.GROUP_REMOVED), anyString());				
		
		// validate change to model
		Snapshot actualSnapshot = snapshotWidget.getSnapshot();
		assertEquals(0, actualSnapshot.getGroups().size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRemoveGroupFailure() throws Exception {		
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);
		reset(mockView);
		Snapshot expectedSnapshot = getSetSnapshotState();
		expectedSnapshot.getGroups().remove(0);
		setupUpdateEntityWhens(expectedSnapshot);
		AsyncMockStubber.callFailureWith(null).when(mockSynapseClient).updateEntity(anyString(), any(AsyncCallback.class));
		
		Snapshot rebuildSnapshot = getSetSnapshotState();
		setupRebuildEverythingCallbacks(rebuildSnapshot);
		snapshotWidget.removeGroup(0);
		
		// verify failure
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_FAILED_PERSIST);
		verifyRebuildEverything(rebuildSnapshot, snapshotWidget.getSnapshot(), 1);
	}


	
	@SuppressWarnings("unchecked")
	@Test
	public void testAddGroupRecord() throws Exception {		
		// read only
		snapshotWidget.setSnapshot(snapshot, false, true);		
		snapshotWidget.addGroupRecord(0, "id", "version", "note");		
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_IN_READ_ONLY_MODE);
		reset(mockView);
		
		// no edit permission
		snapshotWidget.setSnapshot(snapshot, false, false);		
		snapshotWidget.addGroupRecord(0, "id", "version", "note");		
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_NO_EDIT_PERMISSION);
		reset(mockView);
				
		// bad version format
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);		
		snapshotWidget.addGroupRecord(0, "id", "version", "note");
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_INVALID_VERSION_FORMAT);
	}
	
	@Test
	public void testAddGroupRecordGroupIdx() throws Exception {
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);
		reset(mockView);
		
		// group index out of range
		Snapshot rebuildSnapshot = getSetSnapshotState();
		setupRebuildEverythingCallbacks(rebuildSnapshot);
		snapshotWidget.addGroupRecord(1, "id", "1", null);		
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_GENERIC);
		verifyRebuildEverything(rebuildSnapshot, snapshotWidget.getSnapshot(), 1);		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testAddGroupRecordSuccess() throws Exception {
		String entityId = "syn321";
		Long versionNumber = new Long(1);
		String note = "note";
		
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);
		reset(mockView);
		Snapshot expectedSnapshot = getSetSnapshotState();
		SnapshotGroupRecord record = createRecord(entityId, versionNumber, note);
		ArrayList<SnapshotGroupRecord> records = new ArrayList<SnapshotGroupRecord>();
		records.add(record);
		expectedSnapshot.getGroups().get(0).setRecords(records);		
		setupUpdateEntityWhens(expectedSnapshot);
				
		snapshotWidget.addGroupRecord(0, entityId, versionNumber.toString(), note);

		// verify service calls
		verify(mockSynapseClient).updateEntity(anyString(), any(AsyncCallback.class));
		verify(mockView).showInfo(eq(DisplayConstants.ENTRY_ADDED), anyString());				
		
		// validate change to model
		Snapshot actualSnapshot = snapshotWidget.getSnapshot();
		assertEquals(1, actualSnapshot.getGroups().size());
		assertEquals(entityId, actualSnapshot.getGroups().get(0).getRecords().get(0).getEntityReference().getTargetId());
		assertEquals(versionNumber, actualSnapshot.getGroups().get(0).getRecords().get(0).getEntityReference().getTargetVersionNumber());
		assertEquals(note, actualSnapshot.getGroups().get(0).getRecords().get(0).getNote());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testaddGroupRecordFailure() throws Exception {		
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);
		reset(mockView);
		Snapshot expectedSnapshot = getSetSnapshotState();
		expectedSnapshot.getGroups().get(0).setName(testName);
		expectedSnapshot.getGroups().get(0).setDescription(testDesc);	
		setupUpdateEntityWhens(expectedSnapshot);
		AsyncMockStubber.callFailureWith(null).when(mockSynapseClient).updateEntity(anyString(), any(AsyncCallback.class));
		
		Snapshot rebuildSnapshot = getSetSnapshotState();
		setupRebuildEverythingCallbacks(rebuildSnapshot);
		snapshotWidget.addGroupRecord(0, "id", "1", "note");
		
		// verify failure
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_FAILED_PERSIST);
		verifyRebuildEverything(rebuildSnapshot, snapshotWidget.getSnapshot(), 1);
	}

	@Test
	public void testUpdateGroupRecord() throws Exception {		
		// read only
		snapshotWidget.setSnapshot(snapshot, false, true);		
		snapshotWidget.updateGroupRecord(0, 0, "note");		
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_IN_READ_ONLY_MODE);
		reset(mockView);
		
		// no edit permission
		snapshotWidget.setSnapshot(snapshot, false, false);		
		snapshotWidget.updateGroupRecord(0, 0, "note");		
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_NO_EDIT_PERMISSION);
		reset(mockView);
	}
	
	@Test
	public void testUpdateGroupRecordGroupIdx() throws Exception {
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);
		reset(mockView);
		
		// group index out of range
		Snapshot rebuildSnapshot = getSetSnapshotState();
		setupRebuildEverythingCallbacks(rebuildSnapshot);
		snapshotWidget.updateGroupRecord(1, 0, "note");		
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_GENERIC);
		verifyRebuildEverything(rebuildSnapshot, snapshotWidget.getSnapshot(), 1);		
	}
	
	@Test
	public void testUpdateGroupRecordRecordIdx() throws Exception {
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);
		reset(mockView);
		
		// bad row index
		addSingleGroupRecordToSnapshot(snapshot, "id", (long)1, null);
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);		
		Snapshot rebuildSnapshot = getSetSnapshotState();
		setupRebuildEverythingCallbacks(rebuildSnapshot);
		
		snapshotWidget.updateGroupRecord(0, 1, "note");				
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_GENERIC);
		verifyRebuildEverything(rebuildSnapshot, snapshotWidget.getSnapshot(), 1);		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateGroupRecordSuccess() throws Exception {
		String entityId = "syn321";
		Long versionNumber = new Long(1);
		String note = "note";
		String updatedNote = "new note";
		
		// build up pre snapshot
		addSingleGroupRecordToSnapshot(snapshot, entityId, versionNumber, note);		
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);
		reset(mockView);
		
		// build expected		
		Snapshot expectedSnapshot = createDefaultSnapshot();
		addSingleGroupRecordToSnapshot(expectedSnapshot, entityId, versionNumber, updatedNote);
		setupUpdateEntityWhens(expectedSnapshot);
				
		snapshotWidget.updateGroupRecord(0, 0, updatedNote);

		// verify service calls
		verify(mockSynapseClient).updateEntity(anyString(), any(AsyncCallback.class));
		verify(mockView).showInfo(eq(DisplayConstants.UPDATE_SAVED), anyString());				
		
		// validate change to model
		Snapshot actualSnapshot = snapshotWidget.getSnapshot();
		assertEquals(1, actualSnapshot.getGroups().size());
		assertEquals(entityId, actualSnapshot.getGroups().get(0).getRecords().get(0).getEntityReference().getTargetId());
		assertEquals(versionNumber, actualSnapshot.getGroups().get(0).getRecords().get(0).getEntityReference().getTargetVersionNumber());
		assertEquals(updatedNote, actualSnapshot.getGroups().get(0).getRecords().get(0).getNote());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateGroupRecordFailure() throws Exception {	
		addSingleGroupRecordToSnapshot(snapshot, "1", (long)1, "note");		
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);
		reset(mockView);
		Snapshot expectedSnapshot = getSetSnapshotState();
		expectedSnapshot.getGroups().get(0).setName(testName);
		expectedSnapshot.getGroups().get(0).setDescription(testDesc);	
		setupUpdateEntityWhens(expectedSnapshot);
		AsyncMockStubber.callFailureWith(null).when(mockSynapseClient).updateEntity(anyString(), any(AsyncCallback.class));
		
		Snapshot rebuildSnapshot = getSetSnapshotState();
		setupRebuildEverythingCallbacks(rebuildSnapshot);
		snapshotWidget.updateGroupRecord(0, 0, "note");
		
		// verify failure
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_FAILED_PERSIST);
		verifyRebuildEverything(rebuildSnapshot, snapshotWidget.getSnapshot(), 1);
	}

	// --------

	@Test
	public void testRemoveGroupRecord() throws Exception {		
		// read only
		snapshotWidget.setSnapshot(snapshot, false, true);		
		snapshotWidget.removeGroupRecord(0, 0);		
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_IN_READ_ONLY_MODE);
		reset(mockView);
		
		// no edit permission
		snapshotWidget.setSnapshot(snapshot, false, false);		
		snapshotWidget.removeGroupRecord(0, 0);		
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_NO_EDIT_PERMISSION);
		reset(mockView);
	}
	
	@Test
	public void testRemoveGroupRecordGroupIdx() throws Exception {
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);
		reset(mockView);
		
		// group index out of range
		Snapshot rebuildSnapshot = getSetSnapshotState();
		setupRebuildEverythingCallbacks(rebuildSnapshot);
		snapshotWidget.removeGroupRecord(1, 0);		
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_GENERIC);
		verifyRebuildEverything(rebuildSnapshot, snapshotWidget.getSnapshot(), 1);		
	}
	
	@Test
	public void testRemoveGroupRecordRecordIdx() throws Exception {
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);
		reset(mockView);
		
		// bad row index
		addSingleGroupRecordToSnapshot(snapshot, "id", (long)1, null);
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);		
		Snapshot rebuildSnapshot = getSetSnapshotState();
		setupRebuildEverythingCallbacks(rebuildSnapshot);
		
		snapshotWidget.removeGroupRecord(0, 1);				
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_GENERIC);
		verifyRebuildEverything(rebuildSnapshot, snapshotWidget.getSnapshot(), 1);		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testRemoveGroupRecordSuccess() throws Exception {
		String entityId = "syn321";
		Long versionNumber = new Long(1);
		String note = "note";
		String updatedNote = "new note";
		
		// build up pre snapshot
		addSingleGroupRecordToSnapshot(snapshot, entityId, versionNumber, note);		
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);
		reset(mockView);
		
		// build expected		
		Snapshot expectedSnapshot = createDefaultSnapshot();
		addSingleGroupRecordToSnapshot(expectedSnapshot, entityId, versionNumber, updatedNote);
		expectedSnapshot.getGroups().get(0).getRecords().remove(0);
		setupUpdateEntityWhens(expectedSnapshot);
				
		snapshotWidget.removeGroupRecord(0, 0);

		// verify service calls
		verify(mockSynapseClient).updateEntity(anyString(), any(AsyncCallback.class));
		verify(mockView).showInfo(eq(DisplayConstants.ENTRY_REMOVED), anyString());				
		
		// validate change to model
		Snapshot actualSnapshot = snapshotWidget.getSnapshot();
		assertEquals(1, actualSnapshot.getGroups().size());
		assertEquals(0, actualSnapshot.getGroups().get(0).getRecords().size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRemoveGroupRecordFailure() throws Exception {	
		addSingleGroupRecordToSnapshot(snapshot, "1", (long)1, "note");		
		snapshotWidget.setSnapshot(snapshot, CAN_EDIT, READ_ONLY);
		reset(mockView);
		Snapshot expectedSnapshot = getSetSnapshotState();
		expectedSnapshot.getGroups().get(0).setName(testName);
		expectedSnapshot.getGroups().get(0).setDescription(testDesc);	
		setupUpdateEntityWhens(expectedSnapshot);
		AsyncMockStubber.callFailureWith(null).when(mockSynapseClient).updateEntity(anyString(), any(AsyncCallback.class));
		
		Snapshot rebuildSnapshot = getSetSnapshotState();
		setupRebuildEverythingCallbacks(rebuildSnapshot);
		snapshotWidget.removeGroupRecord(0, 0);
		
		// verify failure
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_FAILED_PERSIST);
		verifyRebuildEverything(rebuildSnapshot, snapshotWidget.getSnapshot(), 1);
	}

	

	/*
	 * Helpers
	 */
	
	@SuppressWarnings("unchecked")
	private String setupAddGroup(String testName, String testDesc) throws JSONObjectAdapterException, RestServiceException {
		String snapshotJson1 = snapshot.writeToJSONObject(factory.createNew()).toJSONString();
		Snapshot expectedSnapshot = new Snapshot(factory.createNew(snapshotJson1));
		expectedSnapshot.setGroups(Arrays.asList(new SnapshotGroup[] { new SnapshotGroup(), new SnapshotGroup() }));
		expectedSnapshot.getGroups().get(0).setName(DisplayConstants.CONTENTS);		
		expectedSnapshot.getGroups().get(1).setName(testName);
		expectedSnapshot.getGroups().get(1).setDescription(testDesc);
		expectedSnapshot.getGroups().get(1).setRecords(Arrays.asList(new SnapshotGroupRecord[] {}));
		String expectedJson = expectedSnapshot.writeToJSONObject(factory.createNew()).toJSONString();
		EntityWrapper expectedWrapper = new EntityWrapper(expectedJson, Snapshot.class.getName(), null);
		AsyncMockStubber.callSuccessWith(expectedWrapper).when(mockSynapseClient).updateEntity(anyString(), any(AsyncCallback.class));		
		when(mockNodeModelCreator.createEntity(expectedJson, Snapshot.class.getName())).thenReturn(expectedSnapshot);
		return expectedJson;
	}

	private Snapshot getSetSnapshotState() throws Exception {
		Snapshot snapshot = createDefaultSnapshot();
		SnapshotGroup defaultGroup = new SnapshotGroup();
		defaultGroup.setName(DisplayConstants.CONTENTS);			
		snapshot.setGroups(new ArrayList<SnapshotGroup>(Arrays.asList(new SnapshotGroup[] { defaultGroup })));					
		return snapshot;
	}

	private Snapshot createDefaultSnapshot() {
		Snapshot snapshot = new Snapshot();
		snapshot.setId("syn1234");
		return snapshot;
	}

	private Data getDataEntity(String id, Long version) {
		Data data = new Data();
		data.setId(id);
		data.setName(id);
		data.setVersionNumber(version);
		return data;
	}

	private void addSingleGroupRecordToSnapshot(Snapshot snapshot, String targetId,
			Long targetVersionNumber, String note) {
		SnapshotGroupRecord ssgRecord = new SnapshotGroupRecord();
		Reference ref = new Reference();
		ref.setTargetId(targetId);
		ref.setTargetVersionNumber(targetVersionNumber);
		ssgRecord.setEntityReference(ref);
		ssgRecord.setNote(note);
		SnapshotGroup ssGroup = new SnapshotGroup();
		ArrayList<SnapshotGroup> groups = new ArrayList<SnapshotGroup>();
		groups.add(ssGroup);
		ArrayList<SnapshotGroupRecord> records = new ArrayList<SnapshotGroupRecord>();
		records.add(ssgRecord);
		ssGroup.setRecords(records);		
		snapshot.setGroups(groups);
	}

	@SuppressWarnings("unchecked")
	private void setupRebuildEverythingCallbacks(Snapshot rebuildSnapshot) throws Exception {
		EntityWrapper rebuildWrapper = new EntityWrapper(rebuildSnapshot.writeToJSONObject(factory.createNew()).toJSONString(), Snapshot.class.getName(), null);
		AsyncMockStubber.callSuccessWith(rebuildWrapper).when(mockSynapseClient).getEntity(eq(snapshot.getId()), any(AsyncCallback.class));
		when(mockNodeModelCreator.createEntity(rebuildWrapper, Snapshot.class)).thenReturn(rebuildSnapshot);
	}
	
	@SuppressWarnings("unchecked")
	private void verifyRebuildEverything(Snapshot rebuildSnapshot, Snapshot actualSnapshot, int expectedRebuildSize) {
		verify(mockSynapseClient).getEntity(eq(actualSnapshot.getId()), any(AsyncCallback.class)); // rebuild all
		assertEquals(expectedRebuildSize, actualSnapshot.getGroups().size()); // check backed out changes after failure
		verify(mockView).setSnapshot(rebuildSnapshot, CAN_EDIT, READ_ONLY);		
	}
	
	private void setupUpdateEntityWhens(Snapshot expectedSnapshot) throws Exception {
		EntityWrapper expectedWrapper = new EntityWrapper(expectedSnapshot.writeToJSONObject(factory.createNew()).toJSONString(), Snapshot.class.getName(), null);
		AsyncMockStubber.callSuccessWith(expectedWrapper).when(mockSynapseClient).updateEntity(anyString(), any(AsyncCallback.class));		
		when(mockNodeModelCreator.createEntity(anyString(), eq(Snapshot.class.getName()))).thenReturn(expectedSnapshot);
	}
	
	private SnapshotGroupRecord createRecord(String entityId, Long versionNumber, String note) {
		Reference ref = new Reference();
		ref.setTargetId(entityId);			
		ref.setTargetVersionNumber(versionNumber);

		SnapshotGroupRecord record = new SnapshotGroupRecord();
		record.setEntityReference(ref);
		record.setNote(note);
		return record;
	}

}
