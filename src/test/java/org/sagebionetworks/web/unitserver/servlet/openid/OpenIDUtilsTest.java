package org.sagebionetworks.web.unitserver.servlet.openid;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sagebionetworks.web.server.servlet.openid.OpenIDUtils;

public class OpenIDUtilsTest {
	
	@Test
	public void testCreateRedirectURL() throws Exception {
		
		// I. isGWTMode=false
		
		// basic case
		assertEquals("foobar.com?status=OK&sessionToken=123", 
				OpenIDUtils.createRedirectURL("foobar.com", "123", /*isGWTMode*/false));
		
		// error
		assertEquals("foobar.com?status=OpenIDError", 
				OpenIDUtils.createErrorRedirectURL("foobar.com", /*isGWTMode*/false));
		
		// II. isGWTMode=true
		
		// basic case
		assertEquals("foobar.com/openid#LoginPlace:123", 
				OpenIDUtils.createRedirectURL("foobar.com/openid#LoginPlace", "123", /*isGWTMode*/true));
		
		// error
		assertEquals("foobar.com/openid#LoginPlace:OpenIDError", 
				OpenIDUtils.createErrorRedirectURL("foobar.com/openid#LoginPlace", /*isGWTMode*/true));
		
	
	}

}
