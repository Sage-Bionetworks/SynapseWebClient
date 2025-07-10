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
    // Check the server name in the request - if it matches a subdomain with a CDN (www, staging, tst), then redirect to
    // the CDN at `cdn-<subdomain>.synapse.org`.
    // Otherwise, redirect to the asset hosted by the servlet.

    URL requestUrl = new URL(request.getRequestURL().toString());
    String scheme = getOriginalScheme(request);
    String pathToAsset = request.getPathInfo();

    Matcher matcher = CDN_HOSTS_REGEX.matcher(request.getServerName());

    URL redirectUrl;

    if (matcher.matches()) {
      // Redirect to asset served by CDN
      redirectUrl =
        new URL(
          scheme,
          "cdn-" + request.getServerName(),
          requestUrl.getPort(),
          pathToAsset
        );
    } else {
      // There is no CDN, redirect to the asset served by the Portal
      redirectUrl =
        new URL(
          scheme,
          request.getServerName(),
          requestUrl.getPort(),
          pathToAsset
        );
      logger.warning(
        "Not redirecting to CDN: server name \"" +
        request.getServerName() +
        "\" does not match known server names with CDNs. This is expected in development."
      );
    }

    // Redirect with 302 Found
    response.sendRedirect(redirectUrl.toString());
  }

  private static String getOriginalScheme(HttpServletRequest request) {
    String forwardedProtoHeader = request.getHeader("X-Forwarded-Proto");
    if (forwardedProtoHeader != null) {
      return forwardedProtoHeader;
    }
    return request.getScheme();
  }
}
