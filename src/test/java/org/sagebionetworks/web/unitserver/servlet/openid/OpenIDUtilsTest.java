package org.sagebionetworks.web.unitserver.servlet.openid;

import static org.junit.Assert.assertEquals;

import java.net.URLEncoder;

import org.junit.Test;
import org.sagebionetworks.web.server.servlet.openid.OpenIDUtils;

public class OpenIDUtilsTest {
	
	@Test
	public void testAddQueryParameter() throws Exception {
		String queryParameter = "sessionToken=2u362864826428";
		// simple case
		String url = "https://foo.bar.com";
		assertEquals(url+"?"+queryParameter, OpenIDUtils.addRequestParameter(url, queryParameter));
		
		// adding to existing param's
		url = "https://foo.bar.com?bas=blah";
		assertEquals(url+"&"+queryParameter, OpenIDUtils.addRequestParameter(url, queryParameter));
		
		// inserting BEFORE a fragment
		url = "https://foo.bar.com?bas=blah";
		assertEquals(url+"&"+queryParameter+"#frag", OpenIDUtils.addRequestParameter(url+"#frag", queryParameter));

		// url encoding required
		queryParameter = "sessionToken";
		String queryValue = "2u36286#826.28";
		String urlEncodedQueryValue = URLEncoder.encode(queryValue, "UTF-8");
		url = "https://foo.bar.com";
		assertEquals(url+"?"+queryParameter+"="+urlEncodedQueryValue, OpenIDUtils.addRequestParameter(url, queryParameter+"="+queryValue));
	}

}
