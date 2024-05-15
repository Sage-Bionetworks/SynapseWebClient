package org.sagebionetworks.web.server.servlet.filter;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.WithTokenizers;
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
import org.commonmark.parser.Parser;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.mvp.AppPlaceHistoryMapper;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * This filter modifies the base Portal.html file to inject with GWT place specific information, like Open Graph metadata.
 * Will attempt to ignore bot traffic (handled by CrawlFilter)
 */
public class HtmlInjectionFilter extends OncePerRequestFilter {

  SynapseClientImpl synapseClient = null;
  Template portalHtmlTemplate = null;
  public static final String DEFAULT_PAGE_TITLE = "Synapse | Sage Bionetworks";
  public static final String DEFAULT_PAGE_DESCRIPTION =
    "Synapse is a platform for supporting scientific collaborations centered around shared biomedical data sets.  Our goal is to make biomedical research more transparent, more reproducible, and more accessible to a broader audience of scientists.  Synapse serves as the host site for a variety of scientific collaborations, individual research projects, and DREAM challenges.";
  public static final String DEFAULT_URL = "https://www.synapse.org";

  // Markdown processor
  private static Parser parser = Parser.builder().build();

  public void init(
    SynapseClientImpl synapseClient,
    Template portalHtmlTemplate
  ) {
    this.synapseClient = synapseClient;
    this.portalHtmlTemplate = portalHtmlTemplate;
  }

  private Map<String, String> getDataModel() {
    Map<String, String> dataModel = new HashMap<>();
    dataModel.put("pageTitle", DEFAULT_PAGE_TITLE);
    dataModel.put("pageDescription", DEFAULT_PAGE_DESCRIPTION);
    dataModel.put("pageDescription", DEFAULT_PAGE_DESCRIPTION);
    dataModel.put("ogUrl", DEFAULT_URL);
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
    if (synapseClient == null) {
      //read Portal.html
      Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
      cfg.setServletContextForTemplateLoading(getServletContext(), "/");
      Template template = cfg.getTemplate("Portal.html");
      init(new SynapseClientImpl(), template);
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
        // Get the components needed to construct the URL

        String domain = request.getServerName();
        // customize data model for this particular page
        dataModel.put("ogUrl", url.toString());

        if (path.startsWith("/Synapse")) {
          // index information about the synapse entity
          String entityId = path.substring(path.indexOf(":") + 1);
        } else if (path.startsWith("/Team")) {
          // index team (including members)
          String teamId = path.substring(path.indexOf(":") + 1);
        } else if (path.startsWith("/Profile")) {
          // index team (including members)
          String profileId = path.substring(path.indexOf(":") + 1);
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

  private String getCreatedByString(String userId) throws RestServiceException {
    UserProfile profile = synapseClient.getUserProfile(userId);
    return getUserProfileString(profile);
  }

  private String getUserProfileString(UserProfile profile) {
    StringBuilder createdByBuilder = new StringBuilder();
    createdByBuilder.append(
      "<a href=\"https://www.synapse.org/Profile:" +
      profile.getOwnerId() +
      "\">"
    );
    createdByBuilder.append(getDisplayName(profile));
    createdByBuilder.append(profile.getUserName());
    createdByBuilder.append("</a>");
    return createdByBuilder.toString();
  }

  private String getDisplayName(UserProfile profile) {
    StringBuilder displayNameBuilder = new StringBuilder();
    if (profile.getFirstName() != null) {
      displayNameBuilder.append(profile.getFirstName() + " ");
    }
    if (profile.getLastName() != null) {
      displayNameBuilder.append(profile.getLastName() + " ");
    }
    return displayNameBuilder.toString();
  }

  public void testFilter(
    HttpServletRequest mockRequest,
    HttpServletResponse mockResponse,
    FilterChain mockFilterChain
  ) throws ServletException, IOException {
    doFilterInternal(mockRequest, mockResponse, mockFilterChain);
  }
}
