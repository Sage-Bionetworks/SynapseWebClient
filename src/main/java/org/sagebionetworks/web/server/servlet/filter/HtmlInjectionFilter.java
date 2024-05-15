package org.sagebionetworks.web.server.servlet.filter;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Team;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl;
import org.sagebionetworks.web.server.servlet.UserDataProvider;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * This filter modifies the base Portal.html file to inject with GWT place specific information, like Open Graph metadata.
 * Will attempt to ignore bot traffic (handled by CrawlFilter)
 */
public class HtmlInjectionFilter extends OncePerRequestFilter {

  private static final String OG_URL_KEY = "ogUrl";
  private static final String PAGE_DESCRIPTION_KEY = "pageDescription";
  private static final String PAGE_TITLE_KEY = "pageTitle";
  Template portalHtmlTemplate = null;
  public static final String DEFAULT_PAGE_TITLE = "Synapse | Sage Bionetworks";
  public static final String DEFAULT_PAGE_DESCRIPTION =
    "Synapse is a platform for supporting scientific collaborations centered around shared biomedical data sets.  Our goal is to make biomedical research more transparent, more reproducible, and more accessible to a broader audience of scientists.  Synapse serves as the host site for a variety of scientific collaborations, individual research projects, and DREAM challenges.";
  public static final String DEFAULT_URL = "https://www.synapse.org";

  public void init(Template portalHtmlTemplate) {
    this.portalHtmlTemplate = portalHtmlTemplate;
  }

  private Map<String, String> getDataModel() {
    Map<String, String> dataModel = new HashMap<>();
    dataModel.put(PAGE_TITLE_KEY, DEFAULT_PAGE_TITLE);
    dataModel.put(PAGE_DESCRIPTION_KEY, DEFAULT_PAGE_DESCRIPTION);
    dataModel.put(OG_URL_KEY, DEFAULT_URL);
    return dataModel;
  }

  public static boolean isGWTPlace(String targetPath) {
    // is alias a known GWT place name?  If so, do nothing
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
      init(template);
    }
    String userAgent = request.getHeader("User-Agent");
    boolean isLikelyBot = CrawlFilter.isLikelyBot(userAgent);

    // get the file path being requested
    URL url = new URL(request.getRequestURL().toString());
    String path = url.getPath();

    if (
      !isLikelyBot &&
      (path.equals("/Portal.html") || path.equals("/") || isGWTPlace(path))
    ) {
      Map<String, String> dataModel = getDataModel();

      try {
        // customize data model for this particular page
        dataModel.put(OG_URL_KEY, url.toString());
        try {
          String accessToken = UserDataProvider.getThreadLocalUserToken(
            request
          );
          SynapseClientImpl synapseClient = new SynapseClientImpl();
          synapseClient.setTokenProvider(() -> {
            return accessToken;
          });
          String placeToken = path.substring(path.indexOf(":") + 1);
          if (path.startsWith("/Synapse")) {
            // index information about the synapse entity
            Synapse place = new Synapse(placeToken);
            String entityId = place.getEntityId();
            EntityBundleRequest entityBundleRequest = new EntityBundleRequest();
            entityBundleRequest.setIncludeEntity(true);
            EntityBundle bundle = synapseClient.getEntityBundle(
              entityId,
              entityBundleRequest
            );
            Entity entity = bundle.getEntity();
            String description = "";
            try {
              WikiPage rootPage = synapseClient.getV2WikiPageAsV1(
                new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null)
              );
              description = CrawlFilter.getPlainTextWiki(entityId, rootPage);
            } catch (Exception e) {}

            dataModel.put(PAGE_TITLE_KEY, entity.getName());
            dataModel.put(PAGE_DESCRIPTION_KEY, description);
          } else if (path.startsWith("/Team")) {
            Team place = new Team(placeToken);
            org.sagebionetworks.repo.model.Team team = synapseClient.getTeam(
              place.getTeamId()
            );

            dataModel.put(PAGE_TITLE_KEY, "Team - " + team.getName());
            dataModel.put(PAGE_DESCRIPTION_KEY, team.getDescription());
          } else if (path.startsWith("/Profile")) {
            Profile place = new Profile(placeToken);
            UserProfile profile = synapseClient.getUserProfile(
              place.getUserId()
            );
            dataModel.put(
              PAGE_TITLE_KEY,
              CrawlFilter.getDisplayName(profile) +
              "(" +
              profile.getUserName() +
              ")"
            );
            dataModel.put(PAGE_DESCRIPTION_KEY, profile.getSummary());
          }
        } catch (Exception e) {
          e.printStackTrace();
        }

        StringWriter stringWriter = new StringWriter();
        try {
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

  public void testFilter(
    HttpServletRequest mockRequest,
    HttpServletResponse mockResponse,
    FilterChain mockFilterChain
  ) throws ServletException, IOException {
    doFilterInternal(mockRequest, mockResponse, mockFilterChain);
  }
}
