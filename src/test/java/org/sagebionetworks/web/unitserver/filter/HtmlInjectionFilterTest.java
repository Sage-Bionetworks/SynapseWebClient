package org.sagebionetworks.web.unitserver.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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
import java.net.URLEncoder;
import java.util.Collections;
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
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.annotation.v2.Annotations;
import org.sagebionetworks.repo.model.dao.WikiPageKey;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
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

  @Mock
  DiscussionThreadBundle mockThreadBundle;

  @Mock
  Team mockTeam;

  @Mock
  UserProfile mockUserProfile;

  public static final String USER_NAME = "reinhardt123";
  public static final String FIRST_NAME = "Reinhardt";
  public static final String LAST_NAME = "Wilhelm";
  public static final String SUMMARY = "Hammer for hitting things";

  public static final String TEAM_NAME = "the greatest team name";
  public static final String TEAM_DESCRIPTION = "an average team description";

  public static final String DISCUSSION_THREAD_TITLE = "my test thread title";

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
    when(mockDiscussionForumClient.getThread(anyString()))
      .thenReturn(mockThreadBundle);
    when(mockThreadBundle.getTitle()).thenReturn(DISCUSSION_THREAD_TITLE);
    when(mockSynapseClient.getTeam(anyString())).thenReturn(mockTeam);
    when(mockTeam.getName()).thenReturn(TEAM_NAME);
    when(mockTeam.getDescription()).thenReturn(TEAM_DESCRIPTION);
    when(mockSynapseClient.getUserProfile(anyString()))
      .thenReturn(mockUserProfile);
    when(mockUserProfile.getUserName()).thenReturn(USER_NAME);
    when(mockUserProfile.getFirstName()).thenReturn(FIRST_NAME);
    when(mockUserProfile.getLastName()).thenReturn(LAST_NAME);
    when(mockUserProfile.getSummary()).thenReturn(SUMMARY);
    //by default, set up as a bot request since this will return more
    when(mockRequest.getHeader("User-Agent")).thenReturn("Googlebot/2.1");
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

  @Test
  public void testDiscussionThreadPage()
    throws ServletException, IOException, RestServiceException, SynapseException {
    String synapseID = "syn12345";
    String discussionThreadId = "5555";
    when(mockEntity.getId()).thenReturn(synapseID);
    setRequestURL(
      "https://www.synapse.org/Synapse:" +
      synapseID +
      HtmlInjectionFilter.DISCUSSION_THREAD_ID +
      discussionThreadId
    );

    filter.testFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockPrintWriter).print(stringCaptor.capture());
    String outputString = stringCaptor.getValue();
    assertTrue(outputString.contains(DISCUSSION_THREAD_TITLE));
  }

  @Test
  public void testSearchPage()
    throws ServletException, IOException, RestServiceException, SynapseException {
    String searchTerm = "cancer";
    setRequestURL("https://www.synapse.org/Search:" + searchTerm);

    filter.testFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockPrintWriter).print(stringCaptor.capture());
    String outputString = stringCaptor.getValue();
    assertTrue(outputString.contains(searchTerm));
  }

  @Test
  public void testSearchPageWithQuery()
    throws ServletException, IOException, RestServiceException, SynapseException, JSONObjectAdapterException {
    String searchTerm = "potato";
    SearchQuery query = new SearchQuery();
    query.setQueryTerm(Collections.singletonList(searchTerm));
    String queryJson = EntityFactory.createJSONStringForEntity(query);
    String encodedQueryJson = URLEncoder.encode(queryJson, "UTF-8");
    setRequestURL("https://www.synapse.org/Search:" + encodedQueryJson);

    filter.testFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockPrintWriter).print(stringCaptor.capture());
    String outputString = stringCaptor.getValue();
    assertTrue(outputString.contains(searchTerm));
  }

  @Test
  public void testTeamSearchPage()
    throws ServletException, IOException, RestServiceException, SynapseException {
    String searchTerm = "challenge";
    setRequestURL("https://www.synapse.org/TeamSearch:" + searchTerm);

    filter.testFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockPrintWriter).print(stringCaptor.capture());
    String outputString = stringCaptor.getValue();
    assertTrue(outputString.contains(HtmlInjectionFilter.SEARCHING_FOR_TEAM));
    assertTrue(outputString.contains(searchTerm));
  }

  @Test
  public void testTeamPage()
    throws ServletException, IOException, RestServiceException, SynapseException {
    setRequestURL("https://www.synapse.org/Team:1234");

    filter.testFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockPrintWriter).print(stringCaptor.capture());
    String outputString = stringCaptor.getValue();
    assertTrue(outputString.contains(TEAM_NAME));
    assertTrue(outputString.contains(TEAM_DESCRIPTION));
  }

  @Test
  public void testProfilePage()
    throws ServletException, IOException, RestServiceException, SynapseException {
    setRequestURL("https://www.synapse.org/Profile:1234");

    filter.testFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockPrintWriter).print(stringCaptor.capture());
    String outputString = stringCaptor.getValue();
    assertTrue(outputString.contains(USER_NAME));
    assertTrue(outputString.contains(FIRST_NAME));
    assertTrue(outputString.contains(LAST_NAME));
    assertTrue(outputString.contains(SUMMARY));
  }

  @Test
  public void testProfilePageWithNullSummary()
    throws ServletException, IOException, RestServiceException, SynapseException {
    setRequestURL("https://www.synapse.org/Profile:1234");
    when(mockUserProfile.getSummary()).thenReturn(null);

    filter.testFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockPrintWriter).print(stringCaptor.capture());
    String outputString = stringCaptor.getValue();
    assertTrue(outputString.contains(USER_NAME));
    assertTrue(outputString.contains(FIRST_NAME));
    assertTrue(outputString.contains(LAST_NAME));
  }

  @Test
  public void testViewMyProfilePage()
    throws ServletException, IOException, RestServiceException, SynapseException {
    setRequestURL("https://www.synapse.org/Profile:v");

    filter.testFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockPrintWriter).print(stringCaptor.capture());
    String outputString = stringCaptor.getValue();
    assertTrue(outputString.contains(HtmlInjectionFilter.DEFAULT_PAGE_TITLE));
    assertTrue(
      outputString.contains(HtmlInjectionFilter.DEFAULT_PAGE_DESCRIPTION)
    );
  }

  @Test
  public void testEditMyProfilePage()
    throws ServletException, IOException, RestServiceException, SynapseException {
    setRequestURL("https://www.synapse.org/Profile:edit");

    filter.testFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockPrintWriter).print(stringCaptor.capture());
    String outputString = stringCaptor.getValue();
    assertTrue(outputString.contains(HtmlInjectionFilter.DEFAULT_PAGE_TITLE));
    assertTrue(
      outputString.contains(HtmlInjectionFilter.DEFAULT_PAGE_DESCRIPTION)
    );
  }

  @Test
  public void testMyProfileDashboardPage()
    throws ServletException, IOException, RestServiceException, SynapseException {
    setRequestURL(
      "https://www.synapse.org/Profile:1131050/projects/created_by_me"
    );

    filter.testFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockPrintWriter).print(stringCaptor.capture());
    String outputString = stringCaptor.getValue();
    assertTrue(outputString.contains(HtmlInjectionFilter.DEFAULT_PAGE_TITLE));
    assertTrue(
      outputString.contains(HtmlInjectionFilter.DEFAULT_PAGE_DESCRIPTION)
    );
  }
}
