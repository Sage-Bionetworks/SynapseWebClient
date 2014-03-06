package org.sagebionetworks.web.unitserver.servlet.openid;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseNotFoundException;
import org.sagebionetworks.client.exceptions.SynapseServerException;
import org.sagebionetworks.web.server.servlet.openid.OpenIDUtils;

public class OpenIDUtilsTest {
	
	
	@Test
	public void testCreateRedirectURLUnknownError() throws Exception {		
		String exceptionMessage="Testing exception message is returned";
		String encodedMessage = "Testing+exception+message+is+returned";
		Exception e = new SynapseServerException(404, exceptionMessage);
		
		// I. isGWTMode=false
		
		// basic case
		assertEquals("foobar.com?status=OK&sessionToken=123", 
				OpenIDUtils.createRedirectURL("foobar.com", "123", false));
		
		// error with detailed message, then without
		assertEquals("foobar.com?status=OpenIDError&detailedMessage="+encodedMessage, 
				OpenIDUtils.createErrorRedirectURL("foobar.com", false, e));
		assertEquals("foobar.com?status=OpenIDError", 
				OpenIDUtils.createErrorRedirectURL("foobar.com", false, new SynapseServerException(404)));
		assertEquals("foobar.com?status=OpenIDError", 
				OpenIDUtils.createErrorRedirectURL("foobar.com", false, new SynapseServerException(404, "")));

		
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
