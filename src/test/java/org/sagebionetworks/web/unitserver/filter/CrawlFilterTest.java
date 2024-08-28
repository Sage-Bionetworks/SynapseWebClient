package org.sagebionetworks.web.unitserver.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.server.servlet.filter.CrawlFilter.META_ROBOTS_NOINDEX;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.annotation.v2.Annotations;
import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValue;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.server.servlet.filter.BotHtml;
import org.sagebionetworks.web.server.servlet.filter.CrawlFilter;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CrawlFilterTest {

  @Captor
  ArgumentCaptor<String> stringCaptor;

  @Mock
  SynapseClient mockSynapseClient;

  @Mock
  EntityBundle mockEntityBundle;

  @Mock
  Entity mockEntity;

  @Mock
  Annotations mockAnnotations;

  @Mock
  EntityChildrenResponse mockEntityChildrenResponse;

  @Captor
  ArgumentCaptor<EntityChildrenRequest> entityChildrenRequestCaptor;

  CrawlFilter filter;

  @Before
  public void setUp()
    throws RestServiceException, IOException, SynapseException {
    filter = new CrawlFilter();
    when(
      mockSynapseClient.getEntityBundleV2(
        anyString(),
        any(EntityBundleRequest.class)
      )
    )
      .thenReturn(mockEntityBundle);
    when(mockEntityBundle.getEntity()).thenReturn(mockEntity);
    when(mockEntityBundle.getAnnotations()).thenReturn(mockAnnotations);
    when(mockSynapseClient.getEntityChildren(any(EntityChildrenRequest.class)))
      .thenReturn(mockEntityChildrenResponse);

    filter.init(mockSynapseClient);
  }

  @Test
  public void testGetEntityHtml()
    throws ServletException, IOException, RestServiceException, JSONObjectAdapterException, SynapseException {
    String synapseID = "syn12345";
    String entityName = "my mock entity";
    when(mockEntity.getId()).thenReturn(synapseID);
    when(mockEntity.getName()).thenReturn(entityName);

    BotHtml response = filter.getEntityHtml(mockEntityBundle);

    assertTrue(response.getBody().contains(synapseID));
    assertTrue(response.getBody().contains(entityName));
    assertFalse(response.getHead().contains(META_ROBOTS_NOINDEX));

    // verify we are asking for all entity types, except link
    verify(mockSynapseClient)
      .getEntityChildren(entityChildrenRequestCaptor.capture());
    EntityChildrenRequest entityChildrenRequest =
      entityChildrenRequestCaptor.getValue();
    List<EntityType> entityTypes = entityChildrenRequest.getIncludeTypes();
    for (EntityType type : EntityType.values()) {
      assertEquals(EntityType.link != type, entityTypes.contains(type));
    }
  }

  @Test
  public void testSynapseEntityPageNoIndex()
    throws ServletException, IOException, RestServiceException, JSONObjectAdapterException, SynapseException {
    String synapseID = "syn12345";
    when(mockEntity.getId()).thenReturn(synapseID);
    Map<String, AnnotationsValue> annotationsMap = new HashMap<
      String,
      AnnotationsValue
    >();
    when(mockAnnotations.getAnnotations()).thenReturn(annotationsMap);
    annotationsMap.put("noindex", new AnnotationsValue());

    BotHtml response = filter.getEntityHtml(mockEntityBundle);

    assertTrue(response.getBody().contains(synapseID));
    assertTrue(response.getHead().contains(META_ROBOTS_NOINDEX));
  }
}
