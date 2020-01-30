package org.sagebionetworks.web.unitclient.widget.table.modal.upload;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.table.modal.upload.ContentTypeDelimiter;

public class ContentTypeDelimiterTest {

	@Test
	public void testAllTypes() {
		// should be able to find all types.
		for (ContentTypeDelimiter ctd : ContentTypeDelimiter.values()) {
			assertEquals(ctd, ContentTypeDelimiter.findByContentType(ctd.getContentType().toUpperCase(), null));
		}
	}

	@Test
	public void testAllExtentions() {
		// should be able to find all types.
		for (ContentTypeDelimiter ctd : ContentTypeDelimiter.values()) {
			if (ctd.getExtentions() != null) {
				for (String extention : ctd.getExtentions()) {
					assertEquals(ctd, ContentTypeDelimiter.findByContentType("unknonwn", "foo." + extention.toUpperCase()));
				}
			}
		}
	}

	@Test
	public void testFindNull() {
		assertEquals(ContentTypeDelimiter.TEXT, ContentTypeDelimiter.findByContentType(null, null));
	}

	@Test
	public void testFindUnknownNullName() {
		assertEquals(ContentTypeDelimiter.TEXT, ContentTypeDelimiter.findByContentType("some/kind/of/type", null));
	}
}
