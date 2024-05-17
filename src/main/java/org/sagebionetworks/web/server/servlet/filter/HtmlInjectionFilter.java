package org.sagebionetworks.web.server.servlet.filter;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.dao.WikiPageKey;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Team;
import org.sagebionetworks.web.client.place.TeamSearch;
import org.sagebionetworks.web.server.servlet.DiscussionForumClientImpl;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.SynapseProviderImpl;
import org.sagebionetworks.web.server.servlet.UserDataProvider;
import org.sagebionetworks.web.shared.SearchQueryUtils;
import org.sagebionetworks.web.shared.WebConstants;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * This filter modifies the base Portal.html file to inject with GWT place specific information, like Open Graph metadata.
 * Will include crawler response if bot traffic is detected (via User Agent).
 */
public class HtmlInjectionFilter extends OncePerRequestFilter {

  public static final String SEARCHING_FOR_TEAM = "Searching for Team: ";
  public static final String OG_URL_KEY = "ogUrl";
  public static final String PAGE_DESCRIPTION_KEY = "pageDescription";
  public static final String OG_PAGE_DESCRIPTION_KEY = "ogPageDescription";
  public static final String PAGE_TITLE_KEY = "pageTitle";
  public static final String OG_PAGE_TITLE_KEY = "ogPageTitle";
  public static final String BOT_HEAD_HTML_KEY = "botHeadHtml";
  public static final String BOT_BODY_HTML_KEY = "botBodyHtml";
  public static final String LOADING_DESCRIPTOR_KEY = "loadingObjectDescriptor";

  Template portalHtmlTemplate = null;
  public static final String DEFAULT_PAGE_TITLE = "Synapse | Sage Bionetworks";
  public static final String DEFAULT_PAGE_DESCRIPTION =
    "Synapse is a platform for supporting scientific collaborations centered around shared biomedical data sets.  Our goal is to make biomedical research more transparent, more reproducible, and more accessible to a broader audience of scientists.  Synapse serves as the host site for a variety of scientific collaborations, individual research projects, and DREAM challenges.";
  public static final String DEFAULT_URL = "https://www.synapse.org";
  public static final String DISCUSSION_THREAD_ID = "/discussion/threadId=";

  public static final int MAX_PAGE_TITLE_LENGTH = 70;
  public static final int MAX_PAGE_DESCRIPTION_LENGTH = 200;

  public static final String META_ROBOTS_NOINDEX =
    "<meta name=\"robots\" content=\"noindex\">";
  SynapseClientImpl synapseClient = null;
  DiscussionForumClientImpl discussionForumClient = null;
  JSONObjectAdapter jsonObjectAdapter = null;
  public static final int MAX_CHILD_PAGES = 5;
  public CrawlFilter crawlFilter;
  private SynapseProvider synapseProvider = new SynapseProviderImpl();

  public void init(
    Template portalHtmlTemplate,
    DiscussionForumClientImpl discussionForumClient,
    CrawlFilter crawlFilter
  ) {
    this.portalHtmlTemplate = portalHtmlTemplate;
    this.discussionForumClient = discussionForumClient;
    this.crawlFilter = crawlFilter;
  }

  public static boolean isLikelyBot(String userAgent) {
    return userAgent != null && userAgent.toLowerCase().contains("bot");
  }

  private String truncate(String s, int maxChars) {
    return s.length() > maxChars ? s.substring(0, maxChars - 3) + "..." : s;
  }

  private Map<String, String> addDefaultsToDataModel(
    Map<String, String> dataModel
  ) {
    dataModel.putIfAbsent(PAGE_TITLE_KEY, DEFAULT_PAGE_TITLE);
    dataModel.putIfAbsent(PAGE_DESCRIPTION_KEY, DEFAULT_PAGE_DESCRIPTION);
    dataModel.putIfAbsent(OG_URL_KEY, DEFAULT_URL);
    dataModel.putIfAbsent(BOT_HEAD_HTML_KEY, "");
    dataModel.putIfAbsent(BOT_BODY_HTML_KEY, "");
    dataModel.putIfAbsent(LOADING_DESCRIPTOR_KEY, "Loading");

    dataModel.put(
      OG_PAGE_TITLE_KEY,
      truncate(dataModel.get(PAGE_TITLE_KEY), MAX_PAGE_TITLE_LENGTH)
    );
    dataModel.put(
      OG_PAGE_DESCRIPTION_KEY,
      truncate(dataModel.get(PAGE_DESCRIPTION_KEY), MAX_PAGE_DESCRIPTION_LENGTH)
    );

    return dataModel;
  }

  public static boolean isGWTPlace(String targetPath) {
    //  if it contains a ':', then assume it is a GWT place with token
    return targetPath.contains(":");
    //      Class<? extends PlaceTokenizer<?>>[] placeClasses =
    //          AppPlaceHistoryMapper.class.getAnnotation(WithTokenizers.class).value();
    //        for (Class<? extends PlaceTokenizer<?>> c : placeClasses) {
    //          String simpleName = c.getEnclosingClass().getSimpleName();
    //          if (targetPath.startsWith(simpleName+":"))
    //              return true;
    //        }
    //        return false;
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    if (portalHtmlTemplate == null) {
      //read Portal.html
      Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
      cfg.setServletContextForTemplateLoading(getServletContext(), "/");
      Template template = cfg.getTemplate("Portal.html");
      CrawlFilter crawlFilter = new CrawlFilter();
      init(template, new DiscussionForumClientImpl(), crawlFilter);
    }
    String userAgent = request.getHeader("User-Agent");
    boolean isLikelyBot = isLikelyBot(userAgent);

    // get the file path being requested
    URL url = new URL(request.getRequestURL().toString());
    String uri = request.getRequestURI();
    String path = url.getPath();
    boolean isHomePage =
      path.equals("/Portal.html") ||
      path.equals("/") ||
      path.startsWith("/Home");
    if ((isHomePage || isGWTPlace(path))) {
      Map<String, String> dataModel = new HashMap<>();

      String domain = request.getServerName();
      String lowerCaseDomain = domain.toLowerCase();
      boolean isSynapseTestSite =
        !(lowerCaseDomain.contains("www.synapse.org") ||
          lowerCaseDomain.contains("127.0.0.1"));
      boolean includeBotHtml = isLikelyBot && !isSynapseTestSite;
      try {
        // customize data model for this particular page
        dataModel.put(OG_URL_KEY, url.toString());
        try {
          String accessToken = UserDataProvider.getThreadLocalUserToken(
            request
          );
          SynapseClient synapseClient = synapseProvider.createNewClient();
          crawlFilter.init(synapseClient, discussionForumClient);
          if (accessToken != null) synapseClient.setBearerAuthorizationToken(
            accessToken
          );
          String placeToken = path.substring(path.indexOf(":") + 1);
          if (isHomePage) {
            // use defaults in the dataModel, but also get crawl data if this is a bot
            if (includeBotHtml) {
              dataModel.put(
                BOT_BODY_HTML_KEY,
                crawlFilter.getCachedHomePageHtml()
              );
            }
          } else if (path.startsWith("/Synapse")) {
            Synapse place = new Synapse(placeToken);
            String entityId = place.getEntityId();
            if (uri.contains(DISCUSSION_THREAD_ID)) {
              String threadId = uri.substring(
                uri.indexOf(DISCUSSION_THREAD_ID) +
                DISCUSSION_THREAD_ID.length()
              );
              DiscussionThreadBundle thread = discussionForumClient.getThread(
                threadId
              );
              String discussionThreadContent = "";
              try {
                String discussionThreadUrl = discussionForumClient.getThreadUrl(
                  thread.getMessageKey()
                );
                discussionThreadContent = getURLContents(discussionThreadUrl);
              } catch (Exception e1) {}
              dataModel.put(PAGE_TITLE_KEY, thread.getTitle());
              dataModel.put(PAGE_DESCRIPTION_KEY, discussionThreadContent);

              if (includeBotHtml) {
                dataModel.put(
                  BOT_BODY_HTML_KEY,
                  crawlFilter.getThreadHtml(thread, discussionThreadContent)
                );
              }
            } else {
              // index information about the synapse entity
              EntityBundleRequest bundleRequest = new EntityBundleRequest();
              bundleRequest.setIncludeEntity(true);
              bundleRequest.setIncludeAnnotations(true);

              EntityBundle bundle = synapseClient.getEntityBundleV2(
                entityId,
                bundleRequest
              );
              Entity entity = bundle.getEntity();
              String description = "";
              try {
                WikiPageKey key = new WikiPageKey();
                key.setOwnerObjectId(entityId);
                key.setOwnerObjectType(ObjectType.ENTITY);
                key.setWikiPageId(null);
                WikiPage rootPage = synapseClient.getWikiPage(key);
                description = CrawlFilter.getPlainTextWiki(entityId, rootPage);
              } catch (Exception e) {}

              dataModel.put(PAGE_TITLE_KEY, entity.getName());
              dataModel.put(PAGE_DESCRIPTION_KEY, description);

              if (includeBotHtml) {
                BotHtml botHtml = crawlFilter.getEntityHtml(bundle);
                dataModel.put(BOT_HEAD_HTML_KEY, botHtml.getHead());
                dataModel.put(BOT_BODY_HTML_KEY, botHtml.getBody());
              }
            }
          } else if (path.startsWith("/Search")) {
            // index all projects
            String searchQueryRawValue = uri.substring(uri.indexOf(":") + 1);
            SearchQuery query = SearchQueryUtils.getDefaultSearchQuery();
            try {
              query =
                EntityFactory.createEntityFromJSONString(
                  URLDecoder.decode(searchQueryRawValue, "UTF-8"),
                  SearchQuery.class
                );
            } catch (Exception e) {
              query.setQueryTerm(
                Collections.singletonList(searchQueryRawValue)
              );
            }
            String queryTerm = query.getQueryTerm().get(0);
            if (queryTerm != null && queryTerm.trim().length() > 0) {
              dataModel.put(
                PAGE_TITLE_KEY,
                "Searching for: " + query.getQueryTerm().get(0)
              );
            }

            if (includeBotHtml) {
              dataModel.put(
                BOT_BODY_HTML_KEY,
                crawlFilter.getAllProjectsHtml(query)
              );
            }
          } else if (path.startsWith("/TeamSearch")) {
            TeamSearch place = new TeamSearch(placeToken);
            dataModel.put(
              PAGE_TITLE_KEY,
              SEARCHING_FOR_TEAM + place.getSearchTerm()
            );
            if (includeBotHtml) {
              dataModel.put(
                BOT_BODY_HTML_KEY,
                crawlFilter.getAllTeamsHtml(place.getStart())
              );
            }
          } else if (path.startsWith("/Team")) {
            Team place = new Team(placeToken);
            org.sagebionetworks.repo.model.Team team = synapseClient.getTeam(
              place.getTeamId()
            );

            dataModel.put(PAGE_TITLE_KEY, "Team - " + team.getName());
            dataModel.put(PAGE_DESCRIPTION_KEY, team.getDescription());
            if (includeBotHtml) {
              dataModel.put(BOT_BODY_HTML_KEY, crawlFilter.getTeamHtml(team));
            }
          } else if (path.startsWith("/Profile")) {
            Profile place = new Profile(placeToken);
            String userId = place.getUserId();
            if (
              !userId.equals(Profile.VIEW_PROFILE_TOKEN) &&
              !userId.equals(Profile.EDIT_PROFILE_TOKEN) &&
              place.getArea() == Synapse.ProfileArea.PROFILE
            ) {
              UserProfile profile = synapseClient.getUserProfile(userId);
              dataModel.put(
                PAGE_TITLE_KEY,
                CrawlFilter.getDisplayName(profile) +
                "(" +
                profile.getUserName() +
                ")"
              );
              dataModel.put(PAGE_DESCRIPTION_KEY, profile.getSummary());

              if (includeBotHtml) {
                BotHtml botHtml = crawlFilter.getProfileHtml(profile);
                dataModel.put(BOT_HEAD_HTML_KEY, botHtml.getHead());
                dataModel.put(BOT_BODY_HTML_KEY, botHtml.getBody());
              }
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        // if we have a descriptive page title, then indicate loading object
        if (dataModel.containsKey(PAGE_TITLE_KEY)) {
          dataModel.put(
            LOADING_DESCRIPTOR_KEY,
            "<q>" + dataModel.get(PAGE_TITLE_KEY) + "</q> page is loading"
          );
        }
        StringWriter stringWriter = new StringWriter();
        try {
          addDefaultsToDataModel(dataModel);
          portalHtmlTemplate.process(dataModel, stringWriter);
        } catch (TemplateException e) {
          e.printStackTrace();
        }

        // Get the modified HTML string
        String modifiedHtml = stringWriter.toString();
        // Set response content type
        response.setContentType("text/html");

        // Write the modified HTML string to the response
        PrintWriter out = response.getWriter();
        out.print(modifiedHtml);
        out.flush();
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      filterChain.doFilter(request, response);
    }
  }

  private String getURLContents(String urlTarget) throws IOException {
    URL url = new URL(urlTarget);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    try {
      conn.setRequestProperty(
        WebConstants.CONTENT_TYPE,
        WebConstants.TEXT_PLAIN_CHARSET_UTF8
      );
      InputStream in = new GZIPInputStream(conn.getInputStream());
      try {
        return IOUtils.toString(in, "UTF-8");
      } finally {
        IOUtils.closeQuietly(in);
      }
    } finally {
      conn.disconnect();
    }
  }

  public void testFilter(
    HttpServletRequest mockRequest,
    HttpServletResponse mockResponse,
    FilterChain mockFilterChain
  ) throws ServletException, IOException {
    doFilterInternal(mockRequest, mockResponse, mockFilterChain);
  }

  /**
   * Unit test can override this.
   */
  public void setSynapseProvider(SynapseProvider synapseProvider) {
    this.synapseProvider = synapseProvider;
  }
}
