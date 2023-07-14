package org.sagebionetworks.web.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.repo.model.table.Dataset;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.server.servlet.filter.CrawlFilter;
import org.sagebionetworks.web.shared.WebConstants;

/**
 * Given a dataset entity id, will return json in the form: {"jsonld":
 * "dataset script element content"}
 *
 * @author jay
 *
 */
public class JsonLdContentServlet extends HttpServlet {

  private static Logger logger = Logger.getLogger(
    JsonLdContentServlet.class.getName()
  );
  private static final long serialVersionUID = 1L;

  protected static final ThreadLocal<HttpServletRequest> perThreadRequest =
    new ThreadLocal<HttpServletRequest>();

  private SynapseProvider synapseProvider = new SynapseProviderImpl();
  private TokenProvider tokenProvider = () ->
    UserDataProvider.getThreadLocalUserToken(
      JsonLdContentServlet.perThreadRequest.get()
    );
  private RequestHostProvider requestHostProvider = () ->
    UserDataProvider.getThreadLocalRequestHost(
      JsonLdContentServlet.perThreadRequest.get()
    );

  /**
   * Unit test can override this.
   */
  public void setSynapseProvider(SynapseProvider synapseProvider) {
    this.synapseProvider = synapseProvider;
  }

  /**
   * Unit test uses this to provide a mock token provider
   *
   * @param tokenProvider
   */
  public void setTokenProvider(TokenProvider tokenProvider) {
    this.tokenProvider = tokenProvider;
  }

  @Override
  protected void service(HttpServletRequest arg0, HttpServletResponse arg1)
    throws ServletException, IOException {
    JsonLdContentServlet.perThreadRequest.set(arg0);
    super.service(arg0, arg1);
  }

  @Override
  public void service(ServletRequest arg0, ServletResponse arg1)
    throws ServletException, IOException {
    super.service(arg0, arg1);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    String token = getToken(request);
    SynapseClient synapseClient = createNewClient(token);
    String entityId = request.getParameter(WebConstants.ENTITY_PARAM_KEY);
    PrintWriter out = response.getWriter();
    try {
      response.setContentType("application/json");
      String datasetScriptElementContent = "";
      Entity entity = synapseClient.getEntityById(entityId);
      if (entity instanceof Dataset) {
        WikiPage rootPage = synapseClient.getRootWikiPage(
          entityId,
          ObjectType.ENTITY
        );
        String plainTextWiki = CrawlFilter.getPlainTextWiki(entityId, rootPage);
        EntityBundleRequest bundleRequest = new EntityBundleRequest();
        bundleRequest.setIncludeEntity(true);
        bundleRequest.setIncludeAnnotations(true);
        EntityBundle entityBundle = synapseClient.getEntityBundleV2(
          entityId,
          bundleRequest
        );
        datasetScriptElementContent =
          CrawlFilter.getDatasetScriptElementContent(
            entityBundle,
            plainTextWiki
          );
      }

      out.println(datasetScriptElementContent);
    } catch (Exception e) {
      logger.log(Level.WARNING, e.getMessage(), e);
      response.sendError(
        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        e.getMessage()
      );
    } finally {
      out.close();
    }
  }

  /**
   * Get the access token
   *
   * @param request
   * @return
   */
  public String getToken(final HttpServletRequest request) {
    return tokenProvider.getToken();
  }

  /**
   * Create a new Synapse client.
   *
   * @return
   */
  private SynapseClient createNewClient(String accessToken) {
    SynapseClient client = synapseProvider.createNewClient(
      requestHostProvider.getRequestHost()
    );
    if (accessToken != null) client.setBearerAuthorizationToken(accessToken);
    return client;
  }
}
