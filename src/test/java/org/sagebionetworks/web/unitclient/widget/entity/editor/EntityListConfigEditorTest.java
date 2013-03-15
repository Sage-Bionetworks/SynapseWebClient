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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.EntityGroupRecord;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.EntityGroupRecordDisplay;
import org.sagebionetworks.web.client.widget.entity.editor.EntityListConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.EntityListConfigView;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListUtil;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityListConfigEditorTest {
		
	EntityListConfigEditor editor;
	EntityListConfigView mockView;
	AuthenticationController mockAuthenticationController;
	SynapseClientAsync mockSynapseClient;
	NodeModelCreator mockNodeModelCreator;
	
	Map<String, String> descriptor;
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
		AsyncMockStubber.callSuccessWith(wrapper).when(mockSynapseClient).getEntityForVersion(eq(syn456.getId()), eq(1L), any(AsyncCallback.class));
		when(mockNodeModelCreator.createEntity(wrapper)).thenReturn(syn456);

		// create an entity group record for syn456
		record456 = new EntityGroupRecord();
		Reference ref = new Reference();
		ref.setTargetId(syn456.getId());
		ref.setTargetVersionNumber(1L);
		record456.setEntityReference(ref);
		
		// create empty descriptor
		descriptor = new HashMap<String, String>();		
		
		editor = new EntityListConfigEditor(mockView, mockAuthenticationController, mockSynapseClient, mockNodeModelCreator);
		
		editor.configure(null, descriptor);
	}
	
	@Test
	public void testAsWidget() {
		editor.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigure() throws Exception {
		reset(mockView); // as configure is called in the before
		List<EntityGroupRecord> records = new ArrayList<EntityGroupRecord>();
		records.add(record456);			
		String encoded = EntityListUtil.recordsToString(records);
		descriptor.put(WidgetConstants.ENTITYLIST_WIDGET_LIST_KEY, encoded);
				
		editor.configure(null, descriptor);
		
		verify(mockView).configure();	
		verify(mockView).setEntityGroupRecordDisplay(eq(0), any(EntityGroupRecordDisplay.class), eq(true));
	}
	
	@Test
	public void testAddRecord() throws Exception {		
		editor.addRecord(syn456.getId(), 1L, null);
			
		verify(mockView).setEntityGroupRecordDisplay(eq(0), any(EntityGroupRecordDisplay.class), eq(true));
		List<EntityGroupRecord> records = EntityListUtil.parseRecords(descriptor.get(WidgetConstants.ENTITYLIST_WIDGET_LIST_KEY));
		assertEquals(record456, records.get(0));
	}
	
	@Test
	public void testRemoveRecord() {			
		editor.addRecord(syn456.getId(), 1L, null);
		
		editor.removeRecord(0);
		
		List<EntityGroupRecord> records = EntityListUtil.parseRecords(descriptor.get(WidgetConstants.ENTITYLIST_WIDGET_LIST_KEY));
		assertEquals(0, records.size());
	}
	
	@Test
	public void testUpdateNote() {
		String newNote = "some note";
		editor.addRecord(syn456.getId(), 1L, null);
		
		editor.updateNote(0, newNote);

		List<EntityGroupRecord> records = EntityListUtil.parseRecords(descriptor.get(WidgetConstants.ENTITYLIST_WIDGET_LIST_KEY));
		assertEquals(newNote, records.get(0).getNote());
	}
}












