package org.sagebionetworks.web.unitserver.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.server.servlet.filter.CORSFilter.ORIGIN_HEADER;
import static org.sagebionetworks.web.server.servlet.filter.CORSFilter.SYNAPSE_ORG_SUFFIX;
import static org.sagebionetworks.web.server.servlet.filter.CrawlFilter.ESCAPED_FRAGMENT;
import static org.sagebionetworks.web.server.servlet.filter.CrawlFilter.META_ROBOTS_NOINDEX;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.annotation.v2.Annotations;
import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValue;
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

  @Mock
  EntityChildrenResponse mockEntityChildrenResponse;

  @Mock
  PrintWriter mockPrintWriter;

  @Captor
  ArgumentCaptor<EntityChildrenRequest> entityChildrenRequestCaptor;

  @Before
  public void setUp() throws RestServiceException, IOException {
    filter = new CrawlFilter();
    filter.init(mockSynapseClient, mockDiscussionForumClient);
    when(mockRequest.getHeader(ORIGIN_HEADER))
      .thenReturn("https://www" + SYNAPSE_ORG_SUFFIX);
    when(mockRequest.getServerName()).thenReturn("www" + SYNAPSE_ORG_SUFFIX);
    when(mockRequest.getScheme()).thenReturn("https");
    when(
      mockSynapseClient.getEntityBundle(
        anyString(),
        any(EntityBundleRequest.class)
      )
    )
      .thenReturn(mockEntityBundle);
    when(mockEntityBundle.getEntity()).thenReturn(mockEntity);
    when(mockEntityBundle.getAnnotations()).thenReturn(mockAnnotations);
    when(mockSynapseClient.getEntityChildren(any(EntityChildrenRequest.class)))
      .thenReturn(mockEntityChildrenResponse);
    when(mockResponse.getWriter()).thenReturn(mockPrintWriter);
  }

  @Test
  public void testSynapseEntityPage()
    throws ServletException, IOException, RestServiceException {
    String synapseID = "syn12345";
    String entityName = "my mock entity";
    when(mockEntity.getId()).thenReturn(synapseID);
    when(mockEntity.getName()).thenReturn(entityName);
    when(mockRequest.getQueryString())
      .thenReturn(ESCAPED_FRAGMENT + "Synapse:" + synapseID);

    filter.testFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockPrintWriter).println(stringCaptor.capture());
    String outputString = stringCaptor.getValue();
    assertTrue(outputString.contains(synapseID));
    assertTrue(outputString.contains(entityName));
    assertFalse(outputString.contains(META_ROBOTS_NOINDEX));

    // verify we are asking for all entity types, except link
    verify(mockSynapseClient)
      .getEntityChildren(entityChildrenRequestCaptor.capture());
    EntityChildrenRequest entityChildrenRequest = entityChildrenRequestCaptor.getValue();
    List<EntityType> entityTypes = entityChildrenRequest.getIncludeTypes();
    for (EntityType type : EntityType.values()) {
      assertEquals(EntityType.link != type, entityTypes.contains(type));
    }
  }

  @Test
  public void testSynapseEntityPageNoIndex()
    throws ServletException, IOException {
    String synapseID = "syn12345";
    when(mockEntity.getId()).thenReturn(synapseID);
    when(mockRequest.getQueryString())
      .thenReturn(ESCAPED_FRAGMENT + "Synapse:" + synapseID);
    Map<String, AnnotationsValue> annotationsMap = new HashMap<String, AnnotationsValue>();
    when(mockAnnotations.getAnnotations()).thenReturn(annotationsMap);
    annotationsMap.put("noindex", new AnnotationsValue());

    filter.testFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockPrintWriter).println(stringCaptor.capture());
    String outputString = stringCaptor.getValue();
    assertTrue(outputString.contains(synapseID));
    assertTrue(outputString.contains(META_ROBOTS_NOINDEX));
  }
}
