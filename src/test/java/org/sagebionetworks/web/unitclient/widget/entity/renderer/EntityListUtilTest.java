package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityGroupRecord;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.entity.EntityGroupRecordDisplay;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListUtil;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListUtil.RowLoadedHandler;

public class EntityListUtilTest {

	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Mock
	RowLoadedHandler mockHandler;
	@Mock
	EntityGroupRecord mockRecord;
	@Mock
	Reference mockReference;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(mockRecord.getEntityReference()).thenReturn(mockReference);
	}
	
	@Test
	public void testRecordsToStringRoundTrip() {
		String n1id = "syn123";
		String n2id = "syn456";
		
		List<EntityGroupRecord> records = new ArrayList<EntityGroupRecord>();
		EntityGroupRecord rec = new EntityGroupRecord();
		Reference ref = new Reference();
		ref.setTargetId(n1id);
		ref.setTargetVersionNumber(1L);
		rec.setEntityReference(ref);
		rec.setNote("This is a note with commas, and delimeters!;");
		records.add(rec);
		
		rec = new EntityGroupRecord();
		ref = new Reference();
		ref.setTargetId(n2id);
		rec.setEntityReference(ref);
		records.add(rec);
		
		String encoded = EntityListUtil.recordsToString(records);
		List<EntityGroupRecord> clone = EntityListUtil.parseRecords(encoded);
		assertEquals(records, clone);		
	}
	
	@Test
	public void testLoadIndividualRowDetails() {
		List<EntityGroupRecord> records = new ArrayList<EntityGroupRecord>();
		int rowIndex = 0;
		String xsrfToken = "12222";
		String invalidId = "invalidid";
		boolean isLoggedIn = true;
		records.add(mockRecord);
		when(mockReference.getTargetId()).thenReturn(invalidId);
		
		EntityListUtil.loadIndividualRowDetails(mockSynapseClient, mockSynapseJSNIUtils, isLoggedIn, records, rowIndex, mockHandler, xsrfToken);
		verifyZeroInteractions(mockSynapseClient);
		ArgumentCaptor<EntityGroupRecordDisplay> captor = ArgumentCaptor.forClass(EntityGroupRecordDisplay.class);
		verify(mockHandler).onLoaded(captor.capture());
		EntityGroupRecordDisplay display = captor.getValue();
		String textShown = display.getName().asString();
		assertTrue(textShown.contains(DisplayConstants.ERROR_LOADING));
		assertTrue(textShown.contains(invalidId));
	}
}
