package org.sagebionetworks.web.unitclient.widget.table.modal.upload;

import static org.junit.Assert.*;

import org.junit.Test;
import org.sagebionetworks.web.client.widget.table.modal.upload.ContentTypeDelimiter;

public class ContentTypeDelimiterTest {
	
	@Test
	public void testAllTypes(){
		//  should be able to find all types.
		for(ContentTypeDelimiter ctd: ContentTypeDelimiter.values()){
			assertEquals(ctd, ContentTypeDelimiter.findByContentType(ctd.getContentType().toUpperCase()));
		}
	}

	@Test (expected=IllegalArgumentException.class)
	public void testFindNull(){
		 ContentTypeDelimiter.findByContentType(null);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testFindUnknown(){
		 ContentTypeDelimiter.findByContentType("some/kind/of/type");
	}
}
