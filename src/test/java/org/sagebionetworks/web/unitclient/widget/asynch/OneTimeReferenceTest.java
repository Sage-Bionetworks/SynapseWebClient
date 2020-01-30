package org.sagebionetworks.web.unitclient.widget.asynch;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.asynch.OneTimeReference;

public class OneTimeReferenceTest {

	@Test
	public void testOneTime() {
		String toRef = "a string";
		OneTimeReference<String> oneTime = new OneTimeReference<String>(toRef);
		assertEquals("Must the the reference once", toRef, oneTime.getReference());
		assertEquals("The reference should only be returned once", null, oneTime.getReference());
		assertEquals("The reference should only be returned once", null, oneTime.getReference());
	}

}
