package org.sagebionetworks.web.server.servlet;

import static org.sagebionetworks.web.server.servlet.filter.HtmlInjectionFilter.CDN_HOSTS_REGEX;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.sagebionetworks.web.shared.WebConstants;

/**
 * Handles file handler uploads.
 *
 * @author jay
 *
 */
public class CdnRedirectorServlet extends HttpServlet {

  private static Logger logger = Logger.getLogger(
    CdnRedirectorServlet.class.getName()
  );
  private static final long serialVersionUID = 1L;

  protected static final ThreadLocal<HttpServletRequest> perThreadRequest =
    new ThreadLocal<HttpServletRequest>();

  private RequestHostProvider requestHostProvider = () ->
    UserDataProvider.getThreadLocalRequestHost(
      CdnRedirectorServlet.perThreadRequest.get()
    );

  public void setRequestHostProvider(RequestHostProvider requestHostProvider) {
    this.requestHostProvider = requestHostProvider;
  }

  @Override
  protected void service(HttpServletRequest arg0, HttpServletResponse arg1)
    throws ServletException, IOException {
    CdnRedirectorServlet.perThreadRequest.set(arg0);
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
    // Check the origin - if the origin is `subdomain.synapse.org`, then redirect to the CDN at `cdn-<subdomain>.synapse.org`.
    // Otherwise, redirect to the asset hosted by the servlet.

    URL requestUrl = new URL(getFullURL(request));
    String scheme = getOriginalScheme(request);
    String pathToAsset = request.getPathInfo();
    logger.info(
      "Request received on " + request.getRequestURL() + " for " + pathToAsset
    );

    Matcher matcher = CDN_HOSTS_REGEX.matcher(request.getServerName());

    URL redirectUrl = new URL(
      scheme,
      request.getServerName(),
      requestUrl.getPort(),
      pathToAsset
    );

    if (matcher.matches()) {
      redirectUrl =
        new URL(
          scheme,
          "cdn-" + request.getServerName(),
          requestUrl.getPort(),
          pathToAsset
        );
    } else {
      logger.info(
        request.getServerName() +
        " does not match ^(www|staging|tst)\\.synapse\\.org$"
      );
    }

    response.setHeader( // instruct not to cache
      WebConstants.CACHE_CONTROL_KEY,
      WebConstants.CACHE_CONTROL_VALUE_NO_CACHE
    );
    // Set standard HTTP/1.1 no-cache headers.
    response.setHeader(WebConstants.PRAGMA_KEY, WebConstants.NO_CACHE_VALUE); // Set standard HTTP/1.0 no-cache header.
    response.setDateHeader(WebConstants.EXPIRES_KEY, 0L); // Proxy

    logger.info("Redirecting " + pathToAsset + " to " + redirectUrl);

    // redirect
    response.sendRedirect(redirectUrl.toString());
  }

  public static String getFullURL(HttpServletRequest request) {
    StringBuilder requestURL = new StringBuilder(
      request.getRequestURL().toString()
    );
    String queryString = request.getQueryString();

    if (queryString == null) {
      return requestURL.toString();
    } else {
      return requestURL.append('?').append(queryString).toString();
    }
  }

  private static String getOriginalScheme(HttpServletRequest request) {
    String forwardedProtoHeader = request.getHeader("X-Forwarded-Proto");
    if (forwardedProtoHeader != null) {
      return forwardedProtoHeader;
    }
    return request.getScheme();
  }
}
