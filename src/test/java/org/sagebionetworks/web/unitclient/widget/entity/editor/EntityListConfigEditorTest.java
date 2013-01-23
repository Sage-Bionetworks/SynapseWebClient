package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.EntityGroupRecord;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.widget.EntityListWidgetDescriptor;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.EntityGroupRecordDisplay;
import org.sagebionetworks.web.client.widget.entity.editor.EntityListConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.EntityListConfigView;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityListConfigEditorTest {
		
	EntityListConfigEditor editor;
	EntityListConfigView mockView;
	AuthenticationController mockAuthenticationController;
	SynapseClientAsync mockSynapseClient;
	NodeModelCreator mockNodeModelCreator;
	
	EntityListWidgetDescriptor descriptor;
	Data syn456;
	EntityGroupRecord record456; 
	
	@Before
	public void setup() throws Exception{
		mockView = mock(EntityListConfigView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);

		// create gettable entity
		syn456 = new Data();
		syn456.setId("syn456");
		syn456.setName(syn456.getId());
		EntityWrapper wrapper = new EntityWrapper();		
		AsyncMockStubber.callSuccessWith(wrapper).when(mockSynapseClient).getEntity(eq(syn456.getId()), any(AsyncCallback.class));
		when(mockNodeModelCreator.createEntity(wrapper)).thenReturn(syn456);

		// create an entity group record for syn456
		record456 = new EntityGroupRecord();
		Reference ref = new Reference();
		ref.setTargetId(syn456.getId());
		record456.setEntityReference(ref);
		
		// create empty descriptor
		descriptor = new EntityListWidgetDescriptor();		
		
		editor = new EntityListConfigEditor(mockView, mockAuthenticationController, mockSynapseClient, mockNodeModelCreator);
		
		editor.configure("syn123", descriptor);
	}
	
	@Test
	public void testAsWidget() {
		editor.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigure() throws Exception {
		reset(mockView); // as configure is called in the before
		descriptor.setRecords(new ArrayList<EntityGroupRecord>());
		descriptor.getRecords().add(record456);		
		
		editor.configure("syn123", descriptor);
		
		verify(mockView).configure();	
		verify(mockView).setEntityGroupRecordDisplay(eq(0), any(EntityGroupRecordDisplay.class), eq(true));
	}
	
	@Test
	public void testAddRecord() throws Exception {		
		editor.addRecord(syn456.getId(), null, null);
			
		verify(mockView).setEntityGroupRecordDisplay(eq(0), any(EntityGroupRecordDisplay.class), eq(true));
		assertEquals(record456, descriptor.getRecords().get(0));
	}
	
	@Test
	public void testRemoveRecord() {
		descriptor.setRecords(new ArrayList<EntityGroupRecord>());
		descriptor.getRecords().add(record456);		
		
		editor.removeRecord(0);
		
		assertEquals(0, descriptor.getRecords().size());
	}
	
	@Test
	public void testUpdateNote() {
		String newNote = "some note";
		descriptor.setRecords(new ArrayList<EntityGroupRecord>());
		descriptor.getRecords().add(record456);
		assertFalse(newNote.equals(record456.getNote())); // just to make sure
		
		editor.updateNote(0, newNote);

		assertEquals(newNote, record456.getNote());
	}
}












