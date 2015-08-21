package org.sagebionetworks.web.unitserver.servlet.filter;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.servlet.filter.GWTCacheControlFilter;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class GWTCacheControlFilterTest {
	GWTCacheControlFilter filter;
	HttpServletRequest mockRequest;
	HttpServletResponse mockResponse;
	FilterChain mockFilterChain;
	
	@Before
	public void setUp() {
		mockRequest = mock(HttpServletRequest.class);
		mockResponse = mock(HttpServletResponse.class);
		mockFilterChain = mock(FilterChain.class);
		
		filter = new GWTCacheControlFilter();	
	}
	
	@Test
	public void testDoFilterCacheFiles() throws IOException, ServletException {
		when(mockRequest.getRequestURI()).thenReturn("1.cache.js");
		filter.doFilter(mockRequest, mockResponse, mockFilterChain);
		
		verify(mockResponse).setHeader("Cache-Control", "max-age=28800");
		verify(mockResponse).setDateHeader(eq("Date"), anyLong());
		verify(mockResponse).setDateHeader(eq("Expires"), anyLong());
		verify(mockFilterChain).doFilter(mockRequest, mockResponse);
	}
		
	@Test
	public void testDoFilterNoCache() throws IOException, ServletException {
		when(mockRequest.getRequestURI()).thenReturn("Portal.nocache.js");
		filter.doFilter(mockRequest, mockResponse, mockFilterChain);
		
		verify(mockResponse).setDateHeader("Expires", 0);
		verify(mockResponse).setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, pre-check=0, post-check=0");
		verify(mockResponse).setHeader("Pragma", "no-cache");
		
		verify(mockFilterChain).doFilter(mockRequest, mockResponse);
	}


	@Test
	public void testDoFilterOtherFiles() throws IOException, ServletException {
		when(mockRequest.getRequestURI()).thenReturn("image.jpg");
		filter.doFilter(mockRequest, mockResponse, mockFilterChain);
		
		verify(mockResponse).setHeader("Cache-Control", "");
		verify(mockResponse).setHeader("Pragma", "");
		verify(mockResponse).setDateHeader(eq("Date"), anyLong());
		verify(mockFilterChain).doFilter(mockRequest, mockResponse);
	}
}
