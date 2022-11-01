package org.sagebionetworks.web.unitserver.filter;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.server.servlet.filter.CORSFilter.ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER;
import static org.sagebionetworks.web.server.servlet.filter.CORSFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER;
import static org.sagebionetworks.web.server.servlet.filter.CORSFilter.DEFAULT_ALLOW_ORIGIN;
import static org.sagebionetworks.web.server.servlet.filter.CORSFilter.ORIGIN_HEADER;
import static org.sagebionetworks.web.server.servlet.filter.CORSFilter.SYNAPSE_ORG_SUFFIX;
import static org.sagebionetworks.web.server.servlet.filter.CrawlFilter.*;
import java.io.IOException;
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
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.annotation.v2.Annotations;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.web.server.servlet.DiscussionForumClientImpl;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl;
import org.sagebionetworks.web.server.servlet.filter.CrawlFilter;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

@RunWith(MockitoJUnitRunner.class)
public class CrawlFilterTest {
	CrawlFilter filter;
	@Mock
	HttpServletRequest mockRequest;
	@Mock
	HttpServletResponse mockResponse;
	@Mock
	FilterChain mockFilterChain;
	@Captor
	ArgumentCaptor<String> stringCaptor;
	@Mock
	SynapseClientImpl mockSynapseClient;
	@Mock
	DiscussionForumClientImpl mockDiscussionForumClient;
	@Mock
	EntityBundle mockEntityBundle;
	@Mock
	Entity mockEntity;
	@Mock
	Annotations mockAnnotations;

	@Before
	public void setUp() throws RestServiceException {
		filter = new CrawlFilter();
		filter.init(mockSynapseClient, mockDiscussionForumClient);
		when(mockRequest.getHeader(ORIGIN_HEADER)).thenReturn("https://www" + SYNAPSE_ORG_SUFFIX);
		when(mockRequest.getServerName()).thenReturn("www" + SYNAPSE_ORG_SUFFIX);
		when(mockRequest.getScheme()).thenReturn("https");
		when(mockSynapseClient.getEntityBundle(anyString(), any(EntityBundleRequest.class))).thenReturn(mockEntityBundle);
		when(mockEntityBundle.getEntity()).thenReturn(mockEntity);
		when(mockEntityBundle.getAnnotations()).thenReturn(mockAnnotations);
	}

	@Test
	public void testSynapseEntityPage() throws ServletException, IOException {
		when(mockRequest.getQueryString()).thenReturn(ESCAPED_FRAGMENT + "Synapse:syn12345");
		filter.testFilter(mockRequest, mockResponse, mockFilterChain);

		// verify allow origin header set to the specific origin
		verify(mockResponse).addHeader(eq(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER), stringCaptor.capture());
		String allowOriginHeaderValue = stringCaptor.getValue();
		assertEquals("https://tst.synapse.org", allowOriginHeaderValue);
		// and Access-Control-Allow-Credentials is set to true
		verify(mockResponse).addHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
	}

}
