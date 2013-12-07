package org.sagebionetworks.web.unitserver.servlet.openid;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseNotFoundException;
import org.sagebionetworks.web.server.servlet.openid.OpenIDUtils;

public class OpenIDUtilsTest {
	
	
	@Test
	public void testCreateRedirectURLUnknownError() throws Exception {		
		Exception e = new SynapseException();
		
		// I. isGWTMode=false
		
		// basic case
		assertEquals("foobar.com?status=OK&sessionToken=123", 
				OpenIDUtils.createRedirectURL("foobar.com", "123", false));
		
		// error
		assertEquals("foobar.com?status=OpenIDError", 
				OpenIDUtils.createErrorRedirectURL("foobar.com", false, e));
		
		// II. isGWTMode=true
		
		// basic case
		assertEquals("foobar.com/openid#LoginPlace:123", 
				OpenIDUtils.createRedirectURL("foobar.com/openid#LoginPlace", "123", true));
		
		// error
		assertEquals("foobar.com/openid#LoginPlace:OpenIDError", 
				OpenIDUtils.createErrorRedirectURL("foobar.com/openid#LoginPlace", true, e));
		
	
	}

	@Test
	public void testCreateRedirectURLUnknownUserError() throws Exception {		
		Exception e = new SynapseNotFoundException();
		
		// I. isGWTMode=false
		
		// basic case
		assertEquals("foobar.com?status=OK&sessionToken=123", 
				OpenIDUtils.createRedirectURL("foobar.com", "123", false));
		
		// error
		assertEquals("foobar.com?status=OpenIDUnknownUser", 
				OpenIDUtils.createErrorRedirectURL("foobar.com", false, e));
		
		// II. isGWTMode=true
		
		// basic case
		assertEquals("foobar.com/openid#LoginPlace:123", 
				OpenIDUtils.createRedirectURL("foobar.com/openid#LoginPlace", "123", true));
		
		// error
		assertEquals("foobar.com/openid#LoginPlace:OpenIDUnknownUser", 
				OpenIDUtils.createErrorRedirectURL("foobar.com/openid#LoginPlace", true, e));
		
	
	}

}
