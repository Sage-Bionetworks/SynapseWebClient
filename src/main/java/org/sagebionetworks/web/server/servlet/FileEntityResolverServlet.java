package org.sagebionetworks.web.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.web.server.StackEndpoints;
import org.sagebionetworks.web.shared.WebConstants;

/**
 * Given a File entity id and version, will return json in the form: {"url":
 * "http://www.biodalliance.org/datasets/gencode.bb"}
 *
 * @author jay
 *
 */
public class FileEntityResolverServlet extends HttpServlet {

  private static Logger logger = Logger.getLogger(
    FileEntityResolverServlet.class.getName()
  );
  private static final long serialVersionUID = 1L;

  protected static final ThreadLocal<HttpServletRequest> perThreadRequest = new ThreadLocal<HttpServletRequest>();

  private SynapseProvider synapseProvider = new SynapseProviderImpl();
  private TokenProvider tokenProvider = () ->
    UserDataProvider.getThreadLocalUserToken(
      FileEntityResolverServlet.perThreadRequest.get()
    );
  private RequestHostProvider requestHostProvider = () ->
    UserDataProvider.getThreadLocalRequestHost(
      FileEntityResolverServlet.perThreadRequest.get()
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
    FileEntityResolverServlet.perThreadRequest.set(arg0);
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
    // instruct not to cache
    response.setHeader(
      WebConstants.CACHE_CONTROL_KEY,
      WebConstants.CACHE_CONTROL_VALUE_NO_CACHE
    ); // Set standard HTTP/1.1 no-cache headers.
    response.setHeader(WebConstants.PRAGMA_KEY, WebConstants.NO_CACHE_VALUE); // Set standard HTTP/1.0 no-cache header.
    response.setDateHeader(WebConstants.EXPIRES_KEY, 0L);
    String token = getToken(request);
    SynapseClient client = createNewClient(token);
    String entityId = request.getParameter(WebConstants.ENTITY_PARAM_KEY);
    String entityVersion = request.getParameter(
      WebConstants.ENTITY_VERSION_PARAM_KEY
    );
    PrintWriter out = response.getWriter();
    try {
      response.setContentType("application/json");
      URL resolvedUrl;
      if (entityVersion != null) {
        Long versionNumber = Long.parseLong(entityVersion);
        resolvedUrl =
          client.getFileEntityTemporaryUrlForVersion(entityId, versionNumber);
      } else {
        resolvedUrl =
          client.getFileEntityTemporaryUrlForCurrentVersion(entityId);
      }
      JSONObject json = new JSONObject();
      json.put("url", resolvedUrl.toString());
      out.println(json.toString());
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
