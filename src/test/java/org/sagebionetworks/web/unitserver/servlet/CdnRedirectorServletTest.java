package org.sagebionetworks.web.unitserver.servlet;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseForbiddenException;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.dao.WikiPageKey;
import org.sagebionetworks.repo.model.table.RowReference;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.server.servlet.CdnRedirectorServlet;
import org.sagebionetworks.web.server.servlet.RequestHostProvider;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.unitserver.SynapseClientBaseTest;

public class CdnRedirectorServletTest {

  @Mock
  HttpServletRequest mockRequest;

  @Mock
  HttpServletResponse mockResponse;

  @Mock
  RequestHostProvider mockRequestHostProvider;

  CdnRedirectorServlet servlet;
  ArgumentCaptor<String> redirectUrlCaptor;

  @Before
  public void setup() throws IOException {
    MockitoAnnotations.initMocks(this);
    servlet = new CdnRedirectorServlet();
    servlet.setRequestHostProvider(mockRequestHostProvider);
    redirectUrlCaptor = ArgumentCaptor.forClass(String.class);
  }

  private void setupRequest(
    String protocol,
    String serverName,
    int serverPort,
    String pathInfo
  ) throws IOException {
    StringBuffer requestUrl = new StringBuffer(
      protocol + "://" + serverName + ":" + serverPort + pathInfo
    );
    when(mockRequest.getScheme()).thenReturn(protocol);
    when(mockRequest.getRequestURL()).thenReturn(requestUrl);
    when(mockRequest.getQueryString()).thenReturn(null);
    when(mockRequest.getPathInfo()).thenReturn(pathInfo);
    when(mockRequest.getServerName()).thenReturn(serverName);
    when(mockRequestHostProvider.getRequestHost())
      .thenReturn(serverName + ":" + serverPort);
  }

  @Test
  public void testDoGetSynapseOrg() throws Exception {
    String serverName = "www.synapse.org";
    String pathInfo = "/some/asset.png";
    setupRequest("https", serverName, 80, pathInfo);

    servlet.doGet(mockRequest, mockResponse);

    verify(mockResponse).sendRedirect(redirectUrlCaptor.capture());
    String redirectUrl = redirectUrlCaptor.getValue();
    assertTrue(redirectUrl.startsWith("https://cdn-"));
    assertTrue(redirectUrl.contains(serverName));
    assertTrue(redirectUrl.endsWith(pathInfo));
  }

  @Test
  public void testDoGetStagingSynapseOrg() throws Exception {
    String serverName = "staging.synapse.org";
    String pathInfo = "/some/other/asset.js";
    setupRequest("https", serverName, 80, pathInfo);

    servlet.doGet(mockRequest, mockResponse);

    verify(mockResponse).sendRedirect(redirectUrlCaptor.capture());
    String redirectUrl = redirectUrlCaptor.getValue();
    assertTrue(redirectUrl.startsWith("https://cdn-"));
    assertTrue(redirectUrl.contains(serverName));
    assertTrue(redirectUrl.endsWith(pathInfo));
  }

  @Test
  public void testDoGetLocalhost() throws Exception {
    String serverName = "127.0.0.1";
    int serverPort = 8888;
    String pathInfo = "/my/local/file.txt";
    setupRequest("http", serverName, serverPort, pathInfo);

    servlet.doGet(mockRequest, mockResponse);

    verify(mockResponse).sendRedirect(redirectUrlCaptor.capture());
    String redirectUrl = redirectUrlCaptor.getValue();
    assertTrue(redirectUrl.startsWith("http://"));
    assertTrue(redirectUrl.contains(serverName + ":" + serverPort));
    assertTrue(redirectUrl.endsWith(pathInfo));
  }

  @Test
  public void testNoCacheHeaders() throws Exception {
    setupRequest("https", "www.synapse.org", 80, "/file.css");
    servlet.doGet(mockRequest, mockResponse);

    verify(mockResponse)
      .setHeader(
        eq(WebConstants.CACHE_CONTROL_KEY),
        eq(WebConstants.CACHE_CONTROL_VALUE_NO_CACHE)
      );
    verify(mockResponse)
      .setHeader(eq(WebConstants.PRAGMA_KEY), eq(WebConstants.NO_CACHE_VALUE));
    verify(mockResponse).setDateHeader(eq(WebConstants.EXPIRES_KEY), eq(0L));
  }
}
