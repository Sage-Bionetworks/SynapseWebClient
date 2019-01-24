package org.sagebionetworks.web.unitserver.filter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.sagebionetworks.web.server.servlet.filter.CORSFilter.*;

import java.io.IOException;

import org.sagebionetworks.web.server.servlet.filter.CORSFilter;
import org.sagebionetworks.web.server.servlet.filter.SSLFilter;

@RunWith(MockitoJUnitRunner.class)
public class CORSFilterTest {
	CORSFilter filter;
	@Mock
	HttpServletRequest mockRequest;
	@Mock
	HttpServletResponse mockResponse;
	@Mock
	FilterChain mockFilterChain;
	@Captor
	ArgumentCaptor<String> stringCaptor;
	
	@Before
	public void setUp() {
		filter = new CORSFilter();
		when(mockRequest.getServerName()).thenReturn("tst" + SYNAPSE_ORG_SUFFIX); //tst.synapse.org
		when(mockRequest.getScheme()).thenReturn("https");
		when(mockRequest.getServerPort()).thenReturn(8080);
	}
	
	@Test
	public void testAllowOriginNoCredentials() throws ServletException, IOException{
		//Access-Control-Allow-Credentials not set
		filter.testFilter(mockRequest, mockResponse, mockFilterChain);
		
		//verify allow origin header set to *
		verify(mockResponse).addHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, DEFAULT_ALLOW_ORIGIN);
	}
	
	@Test
	public void testAllowOriginCredentialsFalse() throws ServletException, IOException{
		//Access-Control-Allow-Credentials set to false
		when(mockRequest.getHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER)).thenReturn(Boolean.FALSE.toString());
		
		filter.testFilter(mockRequest, mockResponse, mockFilterChain);
		
		//verify allow origin header set to *
		verify(mockResponse).addHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, DEFAULT_ALLOW_ORIGIN);
	}
	
	@Test
	public void testAllowOriginCredentialsTrueSynapseOrg() throws ServletException, IOException{
		//Access-Control-Allow-Credentials set to true, and we are in a .synapse.org subdomain
		when(mockRequest.getHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER)).thenReturn(Boolean.TRUE.toString());

		filter.testFilter(mockRequest, mockResponse, mockFilterChain);
		
		//verify allow origin header set to the specific origin
		verify(mockResponse).addHeader(eq(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER), stringCaptor.capture());
		String allowOriginHeaderValue = stringCaptor.getValue();
		assertEquals("https://tst.synapse.org:8080", allowOriginHeaderValue);
	}

	@Test
	public void testAllowOriginCredentialsNotSynapseOrg() throws ServletException, IOException{
		//Access-Control-Allow-Credentials set to true, and we are not in a .synapse.org subdomain
		when(mockRequest.getHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER)).thenReturn(Boolean.TRUE.toString());
		when(mockRequest.getServerName()).thenReturn("tst.notsynapse.org");
		
		filter.testFilter(mockRequest, mockResponse, mockFilterChain);
		
		//verify allow origin header set to * (causes CORS preflight to fail browserside)
		verify(mockResponse).addHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, DEFAULT_ALLOW_ORIGIN);
	}
}
