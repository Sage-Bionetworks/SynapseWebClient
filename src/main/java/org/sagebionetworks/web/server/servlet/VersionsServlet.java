package org.sagebionetworks.web.server.servlet;

import com.google.inject.Inject;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

/**
 * Servlet for getting the current repo and portal version
 */
public class VersionsServlet extends HttpServlet {

  private final StackVersionProvider stackVersionProvider;

  @Inject
  public VersionsServlet(StackVersionProvider stackVersionProvider) {
    this.stackVersionProvider = stackVersionProvider;
  }

  private static final long serialVersionUID = 1L;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    // instruct not to cache
    response.setHeader(
      WebConstants.CACHE_CONTROL_KEY,
      WebConstants.CACHE_CONTROL_VALUE_NO_CACHE
    ); // Set standard HTTP/1.1 no-cache headers.
    response.setHeader(WebConstants.PRAGMA_KEY, WebConstants.NO_CACHE_VALUE); // Set standard HTTP/1.0 no-cache header.
    response.setContentType(WebConstants.TEXT_PLAIN_CHARSET_UTF8);
    response.setDateHeader(WebConstants.EXPIRES_KEY, 0L); // Proxy

    response.setStatus(HttpServletResponse.SC_OK);

    try {
      response
        .getOutputStream()
        .write(
          stackVersionProvider
            .get(UserDataProvider.getThreadLocalRequestHost(request))
            .getBytes(StandardCharsets.UTF_8)
        );
      response.getOutputStream().flush();
    } catch (RestServiceException e) {
      // redirect to error place with an entry
      response.sendRedirect(
        FileHandleAssociationServlet.getBaseUrl(request) +
        FileHandleAssociationServlet.ERROR_PLACE +
        URLEncoder.encode(e.getMessage())
      );
    }
  }
}
