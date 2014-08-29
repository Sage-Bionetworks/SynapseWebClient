package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.sagebionetworks.repo.model.EntityGroupRecord;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListUtil;

public class EntityListUtilTest {

	
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
}
