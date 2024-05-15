package org.sagebionetworks.web.unitserver.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.server.servlet.filter.CORSFilter.ORIGIN_HEADER;
import static org.sagebionetworks.web.server.servlet.filter.CORSFilter.SYNAPSE_ORG_SUFFIX;
import static org.sagebionetworks.web.server.servlet.filter.CrawlFilter.META_ROBOTS_NOINDEX;

import freemarker.cache.StringTemplateLoader;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.annotation.v2.Annotations;
import org.sagebionetworks.repo.model.dao.WikiPageKey;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.server.servlet.DiscussionForumClientImpl;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.filter.BotHtml;
import org.sagebionetworks.web.server.servlet.filter.CrawlFilter;
import org.sagebionetworks.web.server.servlet.filter.HtmlInjectionFilter;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

@RunWith(MockitoJUnitRunner.class)
public class HtmlInjectionFilterTest {

  HtmlInjectionFilter filter;

  @Mock
  HttpServletRequest mockRequest;

  @Mock
  HttpServletResponse mockResponse;

  @Mock
  FilterChain mockFilterChain;

  @Captor
  ArgumentCaptor<String> stringCaptor;

  @Mock
  SynapseClient mockSynapseClient;

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

  @Mock
  CrawlFilter mockCrawlFilter;

  @Mock
  SynapseProvider mockSynapseProvider;

  @Mock
  WikiPage mockWikiPage;

  public static final String WIKI_PAGE_MARKDOWN =
    "This is a description of the object";
  public static final String BOT_BODY_HTML =
    "<p> For robot crawlers, in body</p>";
  public static final String BOT_HEAD_HTML =
    "<meta name=\"testforbod\" content=\"some directive\">";

  @Mock
  BotHtml mockBotHtml;

  private Template getTemplate(String html)
    throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException {
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
    StringTemplateLoader stringLoader = new StringTemplateLoader();
    stringLoader.putTemplate("template", html);
    cfg.setTemplateLoader(stringLoader);
    return cfg.getTemplate("template");
  }

  @Before
  public void setUp()
    throws RestServiceException, IOException, SynapseException, JSONObjectAdapterException {
    filter = new HtmlInjectionFilter();
    Template pageTitleTemplate = getTemplate(
      "${" +
      HtmlInjectionFilter.PAGE_TITLE_KEY +
      "} \n${" +
      HtmlInjectionFilter.PAGE_DESCRIPTION_KEY +
      "} \n${" +
      HtmlInjectionFilter.BOT_BODY_HTML_KEY +
      "} \n${" +
      HtmlInjectionFilter.BOT_HEAD_HTML_KEY +
      "}"
    );
    filter.init(pageTitleTemplate, mockDiscussionForumClient, mockCrawlFilter);
    when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapseClient);
    filter.setSynapseProvider(mockSynapseProvider);
    when(mockRequest.getHeader(ORIGIN_HEADER))
      .thenReturn("https://www" + SYNAPSE_ORG_SUFFIX);
    when(mockRequest.getServerName()).thenReturn("www" + SYNAPSE_ORG_SUFFIX);
    when(mockRequest.getScheme()).thenReturn("https");
    when(
      mockSynapseClient.getEntityBundleV2(
        anyString(),
        any(EntityBundleRequest.class)
      )
    )
      .thenReturn(mockEntityBundle);
    when(mockBotHtml.getHead()).thenReturn(BOT_HEAD_HTML);
    when(mockBotHtml.getBody()).thenReturn(BOT_BODY_HTML);

    when(mockCrawlFilter.getEntityHtml(any(EntityBundle.class)))
      .thenReturn(mockBotHtml);
    when(mockSynapseClient.getWikiPage(any(WikiPageKey.class)))
      .thenReturn(mockWikiPage);
    when(mockWikiPage.getMarkdown()).thenReturn(WIKI_PAGE_MARKDOWN);
    when(mockEntityBundle.getEntity()).thenReturn(mockEntity);
    when(mockEntityBundle.getAnnotations()).thenReturn(mockAnnotations);
    when(mockSynapseClient.getEntityChildren(any(EntityChildrenRequest.class)))
      .thenReturn(mockEntityChildrenResponse);
    when(mockResponse.getWriter()).thenReturn(mockPrintWriter);
  }

  private void setRequestURL(String s) {
    StringBuffer sb = new StringBuffer();
    sb.append(s);
    when(mockRequest.getRequestURL()).thenReturn(sb);
    when(mockRequest.getRequestURI()).thenReturn(sb.toString());
  }

  @Test
  public void testSynapseEntityPageAsBot()
    throws ServletException, IOException, RestServiceException, SynapseException {
    when(mockRequest.getHeader("User-Agent")).thenReturn("Googlebot/2.1");
    String synapseID = "syn12345";
    String entityName = "my mock entity";
    when(mockEntity.getId()).thenReturn(synapseID);
    when(mockEntity.getName()).thenReturn(entityName);
    setRequestURL("https://www.synapse.org/Synapse:" + synapseID);

    filter.testFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockPrintWriter).print(stringCaptor.capture());
    String outputString = stringCaptor.getValue();
    assertTrue(outputString.contains(entityName));
    assertTrue(outputString.contains(WIKI_PAGE_MARKDOWN));
    assertFalse(outputString.contains(META_ROBOTS_NOINDEX));
    assertTrue(outputString.contains(BOT_HEAD_HTML));
    assertTrue(outputString.contains(BOT_BODY_HTML));
  }

  @Test
  public void testSynapseEntityPageAsHuman()
    throws ServletException, IOException, RestServiceException {
    when(mockRequest.getHeader("User-Agent"))
      .thenReturn(
        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.111 Safari/537.36"
      );
    String synapseID = "syn12345";
    String entityName = "my mock entity";
    when(mockEntity.getId()).thenReturn(synapseID);
    when(mockEntity.getName()).thenReturn(entityName);
    setRequestURL("https://www.synapse.org/Synapse:" + synapseID);

    filter.testFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockPrintWriter).print(stringCaptor.capture());
    String outputString = stringCaptor.getValue();
    assertTrue(outputString.contains(entityName));
    assertTrue(outputString.contains(WIKI_PAGE_MARKDOWN));
    assertFalse(outputString.contains(META_ROBOTS_NOINDEX));
    assertFalse(outputString.contains(BOT_HEAD_HTML));
    assertFalse(outputString.contains(BOT_BODY_HTML));
  }
}
