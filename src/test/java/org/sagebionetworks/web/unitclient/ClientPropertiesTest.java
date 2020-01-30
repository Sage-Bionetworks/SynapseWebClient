package org.sagebionetworks.web.unitclient;

import static org.junit.Assert.assertEquals;
import static org.sagebionetworks.web.client.ClientProperties.fixResourceToCdnEndpoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.web.client.resources.WebResource;

@RunWith(MockitoJUnitRunner.class)
public class ClientPropertiesTest {
	@Test
	public void testFixResourceToEmptyCdnEndpoint() {
		// in local dev environment, cdn endpoint is an empty string
		String cdnEndpoint = "";
		String rUrl = "js/test.js";
		WebResource r = new WebResource(rUrl);
		// verify no-op
		fixResourceToCdnEndpoint(r, cdnEndpoint);
		assertEquals(rUrl, r.getUrl());

		rUrl = "https://unpkg.com/atest@1.0/test.min.js";
		r.setUrl(rUrl);
		// verify no-op
		fixResourceToCdnEndpoint(r, cdnEndpoint);
		assertEquals(rUrl, r.getUrl());
	}

	@Test
	public void testFixResourceToCdnEndpoint() {
		// in prod/staging environment, cdn endpoint is set
		String cdnEndpoint = "https://cdn.synapse.org/";
		String rUrl = "js/test.js";
		WebResource r = new WebResource(rUrl);
		// verify uses cdn
		fixResourceToCdnEndpoint(r, cdnEndpoint);
		assertEquals(cdnEndpoint + rUrl, r.getUrl());

		rUrl = "https://unpkg.com/atest@1.0/test.min.js";
		r.setUrl(rUrl);
		// verify no-op
		fixResourceToCdnEndpoint(r, cdnEndpoint);
		assertEquals(rUrl, r.getUrl());
	}
}

