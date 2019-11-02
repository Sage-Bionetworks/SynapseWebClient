package org.sagebionetworks.web.unitshared.asynch;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.sagebionetworks.client.AsynchJobType;
import org.sagebionetworks.web.shared.asynch.AsynchType;

public class AsynchTypeTest {

	@Test
	public void testAllTypes() {
		// The names of the AsynchType enumeration must match the java client enumerations AsynchJobType
		for (AsynchJobType clientType : AsynchJobType.values()) {
			try {
				AsynchType sharedTyped = AsynchType.valueOf(clientType.name());
				assertNotNull(sharedTyped);
			} catch (IllegalArgumentException e) {
				fail("Failed to find a matching type for: " + clientType.name());
			}
		}
	}
}
