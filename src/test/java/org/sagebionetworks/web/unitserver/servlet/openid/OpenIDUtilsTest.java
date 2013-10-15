package org.sagebionetworks.web.unitserver.servlet.openid;

import java.net.URLEncoder;

import org.junit.Test;
import org.sagebionetworks.web.server.servlet.openid.OpenIDUtils;

import static org.junit.Assert.*;

public class OpenIDUtilsTest {
	
	@Test
	public void testCreateRedirectURL() throws Exception {
		
		// I. isGWTMode=false
		
		// basic case
		assertEquals("foobar.com?status=OK&sessionToken=123", 
				OpenIDUtils.createRedirectURL("foobar.com", "123", /*accepts tou*/true, /*isGWTMode*/false));

		// tou needed
		assertEquals("foobar.com?status=TermsOfUseAcceptanceRequired", 
				OpenIDUtils.createRedirectURL("foobar.com", "123", /*accepts tou*/false, /*isGWTMode*/false));
		
		// error
		assertEquals("foobar.com?status=OpenIDError", 
				OpenIDUtils.createErrorRedirectURL("foobar.com", /*isGWTMode*/false));
		
		// II. isGWTMode=true
		
		// basic case
		assertEquals("foobar.com/openid#LoginPlace:123", 
				OpenIDUtils.createRedirectURL("foobar.com/openid#LoginPlace", "123", /*accepts tou*/true, /*isGWTMode*/true));

		// tou needed
		assertEquals("foobar.com/openid#LoginPlace:TermsOfUseAcceptanceRequired", 
				OpenIDUtils.createRedirectURL("foobar.com/openid#LoginPlace", "123", /*accepts tou*/false, /*isGWTMode*/true));
		
		// error
		assertEquals("foobar.com/openid#LoginPlace:OpenIDError", 
				OpenIDUtils.createErrorRedirectURL("foobar.com/openid#LoginPlace", /*isGWTMode*/true));
		
	
	}

}
